package com.lifequest.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.model.dto.ChatMessageRequest;
import com.lifequest.model.dto.ChatReplyResponse;

// One reply. The client does not send prior messages, and nothing is stored.
@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private static final int MAX_MESSAGE_LENGTH = 2000;
	private static final String SYSTEM = """
			Eres un consejero de LifeQuest. Respondes en español.
			Usa solo el contexto adjunto. No inventes tareas ni desafíos.
			No tienes mensajes anteriores: cada petición llega sola.
			Esta versión no puede crear ni cambiar nada; si te lo piden, dilo.
			""";

	private final List<ChatContextContributor> contributors;
	private final GroqClient groqClient;

	public ChatService(List<ChatContextContributor> contributors, GroqClient groqClient) {
		this.contributors = contributors;
		this.groqClient = groqClient;
	}

	public ChatReplyResponse reply(Long userId, ChatMessageRequest request) {
		String message = request == null || request.message() == null ? "" : request.message().trim();
		if (message.isEmpty()) {
			throw new InvalidRequestException("message is required");
		}
		if (message.length() > MAX_MESSAGE_LENGTH) {
			throw new InvalidRequestException("message must be at most 2000 characters");
		}
		return new ChatReplyResponse(groqClient.reply(SYSTEM + "\n" + context(userId), message));
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
