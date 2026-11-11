package com.lifequest.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.lifequest.exception.ChatUnavailableException;
import com.lifequest.exception.EmptyGroqReplyException;
import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.InvalidStateException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.ChallengeRequestDto;
import com.lifequest.model.dto.ChatLine;
import com.lifequest.model.dto.ChatMessageRequest;
import com.lifequest.model.dto.ChatProposalView;
import com.lifequest.model.dto.ChatReplyResponse;
import com.lifequest.model.dto.ChatThreadDetail;
import com.lifequest.model.dto.ChatThreadSummary;
import com.lifequest.model.dto.TaskRequestDto;
import com.lifequest.model.entity.ChatMessage;
import com.lifequest.model.entity.ChatProposal;
import com.lifequest.model.entity.ChatThread;
import com.lifequest.repository.ChatMessageRepository;
import com.lifequest.repository.ChatProposalRepository;
import com.lifequest.repository.ChatThreadRepository;
import com.lifequest.service.context.ChatContextContributor;
import com.lifequest.service.tool.ChatTool;

import tools.jackson.databind.json.JsonMapper;

// One thread per conversation. The user message, the reply, and its proposals are stored together, only after Groq returns.
@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private static final int MAX_MESSAGE_LENGTH = 2000;
	private static final int TITLE_LIMIT = 32;
	private static final int PREVIEW_LIMIT = 80;
	private static final int HISTORY_LIMIT = 20;
	private static final String ROLE_USER = "user";
	private static final String ROLE_ASSISTANT = "assistant";
	private static final String STATUS_PENDING = "pending";
	private static final String STATUS_CREATED = "created";
	private static final String STATUS_DISCARDED = "discarded";
	private static final String KIND_TASK = "task";
	private static final String KIND_CHALLENGE = "challenge";
	static final int MAX_PROPOSALS = 10;
	private static final Pattern TASK_LINK_LINE = Pattern.compile(
			"(?ium)^[ \\t>*-]*\\**\\s*relacionad[oa] con la tarea\\**\\s*:?\\s*id\\s*\\d+.*(?:\\R|$)");
	private static final Pattern TASK_LINK_INLINE = Pattern.compile(
			"(?iu)\\s*\\**relacionad[oa] con la tarea\\**\\s*:?\\s*id\\s*\\d+\\s*(?:\\([^)]*\\))?");
	private static final String SYSTEM = """
			Eres un consejero de LifeQuest. Respondes en español.
			Usa solo el contexto adjunto. No inventes tareas ni desafíos.
			Puedes proponer una tarea (misión) o un desafío solo llamando a las funciones. Una propuesta no está creada hasta que el usuario pulse Crear. No digas que ya la creaste.
			La tarjeta con Crear solo existe si la función responde que la propuesta quedó pendiente. Si devuelve un error, corrige los datos y vuelve a llamarla antes de responder. No copies la propuesta en una lista y no digas que pulse Crear si la función no tuvo éxito. No digas que preparaste un desafío si propose_challenge no tuvo éxito.
			Si la hora de inicio ya pasó, llama a propose_task una sola vez con una hora de hoy que aún no haya empezado, o con el día siguiente. Si aun así envías la hora pasada, la propuesta se guarda con una hora posterior.
			Un desafío se apoya en una tarea que el usuario ya tiene: pasa su id solo en la función. En el mensaje no digas el id, la fecha ni la hora de esa tarea.
			Los desafíos diarios de la home se calculan solos; no los propongas.
			No edites ni borres nada.
			""";

	private final List<ChatContextContributor> contributors;
	private final List<ChatTool> tools;
	private final Map<String, ChatTool> toolsByName;
	private final GroqClient groqClient;
	private final ChatThreadRepository threadRepository;
	private final ChatMessageRepository messageRepository;
	private final ChatProposalRepository proposalRepository;
	private final TaskService taskService;
	private final ChallengeService challengeService;
	private final JsonMapper jsonMapper;
	private final Clock clock;
	private final TransactionTemplate transactionTemplate;

	public ChatService(
			List<ChatContextContributor> contributors,
			List<ChatTool> tools,
			GroqClient groqClient,
			ChatThreadRepository threadRepository,
			ChatMessageRepository messageRepository,
			ChatProposalRepository proposalRepository,
			TaskService taskService,
			ChallengeService challengeService,
			JsonMapper jsonMapper,
			Clock clock,
			PlatformTransactionManager transactionManager) {
		this.contributors = contributors;
		this.tools = tools;
		this.toolsByName = tools.stream().collect(Collectors.toMap(ChatTool::name, Function.identity()));
		this.groqClient = groqClient;
		this.threadRepository = threadRepository;
		this.messageRepository = messageRepository;
		this.proposalRepository = proposalRepository;
		this.taskService = taskService;
		this.challengeService = challengeService;
		this.jsonMapper = jsonMapper;
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
		Map<Long, List<ChatProposalView>> proposals = proposalRepository
				.findByThreadIdAndUserIdOrderByIdAsc(thread.getId(), userId).stream()
				.collect(Collectors.groupingBy(ChatProposal::getMessageId, Collectors.mapping(ChatService::toView, Collectors.toList())));
		List<ChatLine> messages = messageRepository.findByThreadIdOrderByIdAsc(thread.getId()).stream()
				.map(message -> new ChatLine(
						message.getRole(),
						visible(message.getRole(), message.getContent()),
						proposals.getOrDefault(message.getId(), List.of())))
				.toList();
		return new ChatThreadDetail(thread.getId(), thread.getTitle(), messages);
	}

	public ChatReplyResponse reply(Long userId, ChatMessageRequest request) {
		String message = message(request);
		Long threadId = request == null ? null : request.threadId();
		List<GroqClient.Turn> history = history(userId, threadId);
		List<ChatTool.Draft> drafts = new ArrayList<>();
		String reply = answer(userId, history, message, drafts);
		ChatThread saved = transactionTemplate.execute(status -> store(userId, threadId, message, reply, drafts));
		return new ChatReplyResponse(reply, saved.getId());
	}

	@Transactional
	public ChatProposalView confirm(Long userId, Long proposalId) {
		ChatProposal proposal = pending(userId, proposalId);
		if (KIND_TASK.equals(proposal.getKind())) {
			taskService.create(userId, read(proposal.getPayload(), TaskRequestDto.class));
		} else if (KIND_CHALLENGE.equals(proposal.getKind())) {
			challengeService.create(userId, read(proposal.getPayload(), ChallengeRequestDto.class));
		} else {
			throw new InvalidRequestException("Proposal kind is invalid");
		}
		proposal.setStatus(STATUS_CREATED);
		return toView(proposalRepository.save(proposal));
	}

	@Transactional
	public ChatProposalView discard(Long userId, Long proposalId) {
		ChatProposal proposal = pending(userId, proposalId);
		proposal.setStatus(STATUS_DISCARDED);
		return toView(proposalRepository.save(proposal));
	}

	private ChatProposal pending(Long userId, Long proposalId) {
		ChatProposal proposal = proposalRepository.findByIdAndUserId(proposalId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
		if (!STATUS_PENDING.equals(proposal.getStatus())) {
			throw new InvalidStateException("Proposal is no longer pending");
		}
		return proposal;
	}

	private String answer(Long userId, List<GroqClient.Turn> history, String message, List<ChatTool.Draft> drafts) {
		try {
			return visible(ROLE_ASSISTANT, groqClient.reply(
					instructions() + "\n" + context(userId),
					history,
					message,
					tools.stream().map(ChatTool::spec).toList(),
					(name, arguments) -> callTool(userId, name, arguments, drafts)));
		} catch (EmptyGroqReplyException ex) {
			if (drafts.isEmpty()) {
				throw ex;
			}
			return "La propuesta quedó pendiente. Pulsa Crear para guardarla.";
		} catch (ChatUnavailableException ex) {
			if (drafts.isEmpty()) {
				throw ex;
			}
			if (drafts.size() >= MAX_PROPOSALS) {
				return "Puedo proponer como máximo " + MAX_PROPOSALS
						+ " cosas por mensaje. Pídeme el resto en otro mensaje.";
			}
			String noun = drafts.size() == 1 ? "propuesta pendiente" : "propuestas pendientes";
			return "He dejado " + drafts.size() + " " + noun + ". Pídeme el resto en otro mensaje.";
		}
	}

	private String callTool(Long userId, String name, String arguments, List<ChatTool.Draft> drafts) {
		if (drafts.size() >= MAX_PROPOSALS) {
			return "Stop. The maximum is " + MAX_PROPOSALS
					+ " proposals in one reply. Do not call tools again. Tell the user that maximum.";
		}
		ChatTool tool = toolsByName.get(name);
		if (tool == null) {
			return "Unknown tool";
		}
		try {
			ChatTool.Outcome outcome = tool.call(userId, arguments);
			if (outcome.draft() != null) {
				drafts.add(outcome.draft());
			}
			return outcome.message();
		} catch (RuntimeException ex) {
			log.warn("Tool {} failed ({})", name, ex.getClass().getSimpleName());
			return "Tool failed";
		}
	}

	private <T> T read(String payload, Class<T> type) {
		try {
			return jsonMapper.readValue(payload, type);
		} catch (RuntimeException ex) {
			throw new InvalidRequestException("Proposal payload is invalid");
		}
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

	private ChatThread store(Long userId, Long threadId, String message, String reply, List<ChatTool.Draft> drafts) {
		Instant now = clock.instant();
		ChatThread thread = threadId == null ? create(userId, message, now) : touch(userId, threadId, now);
		saveMessage(thread.getId(), ROLE_USER, message, now);
		ChatMessage assistant = saveMessage(thread.getId(), ROLE_ASSISTANT, reply, now);
		for (ChatTool.Draft draft : drafts) {
			saveProposal(userId, thread.getId(), assistant.getId(), draft, now);
		}
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

	private ChatMessage saveMessage(Long threadId, String role, String content, Instant now) {
		ChatMessage row = new ChatMessage();
		row.setThreadId(threadId);
		row.setRole(role);
		row.setContent(content);
		row.setCreatedAt(now);
		return messageRepository.save(row);
	}

	private void saveProposal(Long userId, Long threadId, Long messageId, ChatTool.Draft draft, Instant now) {
		ChatProposal row = new ChatProposal();
		row.setThreadId(threadId);
		row.setUserId(userId);
		row.setMessageId(messageId);
		row.setKind(draft.kind());
		row.setTitle(draft.title());
		row.setWhenLabel(draft.whenLabel());
		row.setWhy(draft.why());
		row.setPayload(draft.payload());
		row.setStatus(STATUS_PENDING);
		row.setCreatedAt(now);
		proposalRepository.save(row);
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

	private static ChatProposalView toView(ChatProposal proposal) {
		return new ChatProposalView(
				proposal.getId(),
				proposal.getKind(),
				proposal.getTitle(),
				proposal.getWhenLabel(),
				proposal.getWhy(),
				proposal.getStatus());
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

	private static String visible(String role, String content) {
		if (!ROLE_ASSISTANT.equals(role) || content == null || content.isBlank()) {
			return content;
		}
		String text = TASK_LINK_LINE.matcher(content).replaceAll("");
		text = TASK_LINK_INLINE.matcher(text).replaceAll("");
		return text.replaceAll("\\n{3,}", "\n\n").trim();
	}

	private static String cut(String value, int maxChars) {
		if (value.codePointCount(0, value.length()) <= maxChars) {
			return value;
		}
		return value.substring(0, value.offsetByCodePoints(0, maxChars));
	}

	private String instructions() {
		ZonedDateTime now = ZonedDateTime.now(clock);
		return SYSTEM + "\nEn un mismo mensaje puedes proponer como máximo " + MAX_PROPOSALS
				+ " cosas, entre misiones y desafíos. Si piden más, dilo y propone solo " + MAX_PROPOSALS
				+ ". No sigas llamando a las funciones.\nAhora es "
				+ now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
				+ " " + now.getZone() + ".";
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
