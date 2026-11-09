package com.lifequest.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.ChatLine;
import com.lifequest.model.dto.ChatMessageRequest;
import com.lifequest.model.dto.ChatReplyResponse;
import com.lifequest.model.dto.ChatThreadDetail;
import com.lifequest.model.dto.ChatThreadSummary;
import com.lifequest.model.entity.ChatMessage;
import com.lifequest.model.entity.ChatThread;
import com.lifequest.repository.ChatMessageRepository;
import com.lifequest.repository.ChatThreadRepository;
import com.lifequest.service.context.ChatContextContributor;

// One thread per conversation. The user message and the reply are stored together, only after Groq returns.
@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private static final int MAX_MESSAGE_LENGTH = 2000;
	private static final int TITLE_LIMIT = 32;
	private static final int PREVIEW_LIMIT = 80;
	private static final int HISTORY_LIMIT = 20;
	private static final String ROLE_USER = "user";
	private static final String ROLE_ASSISTANT = "assistant";
	private static final String SYSTEM = """
			Eres un consejero de LifeQuest. Respondes en español.
			Usa solo el contexto adjunto. No inventes tareas ni desafíos.
			Esta versión no puede crear ni cambiar nada; si te lo piden, dilo.
			""";

	private final List<ChatContextContributor> contributors;
	private final GroqClient groqClient;
	private final ChatThreadRepository threadRepository;
	private final ChatMessageRepository messageRepository;
	private final Clock clock;
	private final TransactionTemplate transactionTemplate;

	public ChatService(
			List<ChatContextContributor> contributors,
			GroqClient groqClient,
			ChatThreadRepository threadRepository,
			ChatMessageRepository messageRepository,
			Clock clock,
			PlatformTransactionManager transactionManager) {
		this.contributors = contributors;
		this.groqClient = groqClient;
		this.threadRepository = threadRepository;
		this.messageRepository = messageRepository;
		this.clock = clock;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Transactional(readOnly = true)
	public List<ChatThreadSummary> list(Long userId) {
		return threadRepository.findByUserIdOrderByUpdatedAtDescIdDesc(userId).stream()
				.map(thread -> new ChatThreadSummary(
						thread.getId(),
						thread.getTitle(),
						preview(thread.getId()),
						thread.getUpdatedAt()))
				.toList();
	}

	@Transactional(readOnly = true)
	public ChatThreadDetail get(Long userId, Long threadId) {
		ChatThread thread = owned(userId, threadId);
		List<ChatLine> messages = messageRepository.findByThreadIdOrderByIdAsc(thread.getId()).stream()
				.map(message -> new ChatLine(message.getRole(), message.getContent()))
				.toList();
		return new ChatThreadDetail(thread.getId(), thread.getTitle(), messages);
	}

	public ChatReplyResponse reply(Long userId, ChatMessageRequest request) {
		String message = message(request);
		Long threadId = request == null ? null : request.threadId();
		List<GroqClient.Turn> history = history(userId, threadId);
		String reply = groqClient.reply(SYSTEM + "\n" + context(userId), history, message);
		ChatThread saved = transactionTemplate.execute(status -> store(userId, threadId, message, reply));
		return new ChatReplyResponse(reply, saved.getId());
	}

	private List<GroqClient.Turn> history(Long userId, Long threadId) {
		if (threadId == null) {
			return List.of();
		}
		owned(userId, threadId);
		List<ChatMessage> latest = new ArrayList<>(messageRepository.findByThreadIdOrderByIdDesc(
				threadId, PageRequest.of(0, HISTORY_LIMIT)));
		Collections.reverse(latest);
		return latest.stream()
				.map(row -> new GroqClient.Turn(row.getRole(), row.getContent()))
				.toList();
	}

	private ChatThread store(Long userId, Long threadId, String message, String reply) {
		Instant now = clock.instant();
		ChatThread thread = threadId == null ? create(userId, message, now) : touch(userId, threadId, now);
		saveMessage(thread.getId(), ROLE_USER, message, now);
		saveMessage(thread.getId(), ROLE_ASSISTANT, reply, now);
		return thread;
	}

	private ChatThread create(Long userId, String message, Instant now) {
		ChatThread thread = new ChatThread();
		thread.setUserId(userId);
		thread.setTitle(cut(message, TITLE_LIMIT));
		thread.setCreatedAt(now);
		thread.setUpdatedAt(now);
		return threadRepository.save(thread);
	}

	private ChatThread touch(Long userId, Long threadId, Instant now) {
		ChatThread thread = owned(userId, threadId);
		thread.setUpdatedAt(now);
		return threadRepository.save(thread);
	}

	private void saveMessage(Long threadId, String role, String content, Instant now) {
		ChatMessage row = new ChatMessage();
		row.setThreadId(threadId);
		row.setRole(role);
		row.setContent(content);
		row.setCreatedAt(now);
		messageRepository.save(row);
	}

	private ChatThread owned(Long userId, Long threadId) {
		return threadRepository.findByIdAndUserId(threadId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
	}

	private String preview(Long threadId) {
		return messageRepository.findFirstByThreadIdOrderByIdDesc(threadId)
				.map(message -> cut(message.getContent(), PREVIEW_LIMIT))
				.orElse("");
	}

	private static String message(ChatMessageRequest request) {
		String message = request == null || request.message() == null ? "" : request.message().trim();
		if (message.isEmpty()) {
			throw new InvalidRequestException("message is required");
		}
		if (message.length() > MAX_MESSAGE_LENGTH) {
			throw new InvalidRequestException("message must be at most 2000 characters");
		}
		return message;
	}

	private static String cut(String value, int maxChars) {
		if (value.codePointCount(0, value.length()) <= maxChars) {
			return value;
		}
		return value.substring(0, value.offsetByCodePoints(0, maxChars));
	}

	private String context(Long userId) {
		return contributors.stream()
				.map(contributor -> block(contributor, userId))
				.collect(Collectors.joining("\n"));
	}

	private static String block(ChatContextContributor contributor, Long userId) {
		try {
			return contributor.text(userId);
		} catch (RuntimeException ex) {
			log.warn("Chat context failed ({})", contributor.getClass().getSimpleName());
			return "no disponible";
		}
	}
}
