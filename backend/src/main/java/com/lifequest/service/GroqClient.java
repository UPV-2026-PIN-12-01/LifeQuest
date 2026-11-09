package com.lifequest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lifequest.config.GroqProperties;
import com.lifequest.exception.ChatUnavailableException;

@Service
public class GroqClient {

	private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
	private static final int MAX_TOKENS = 600;
	private static final double TEMPERATURE = 0.3;

	private final RestClient restClient;
	private final GroqProperties properties;

	public GroqClient(@Qualifier("groqRestClient") RestClient restClient, GroqProperties properties) {
		this.restClient = restClient;
		this.properties = properties;
	}

	// history is this thread only, oldest first, and does not include userMessage.
	public String reply(String system, List<Turn> history, String userMessage) {
		if (isBlank(properties.apiKey()) || isBlank(properties.model()) || isBlank(properties.url())) {
			log.warn("Groq is not configured");
			throw new ChatUnavailableException();
		}
		try {
			// Low effort keeps the reply inside max_tokens; gpt-oss spends that budget on reasoning first.
			GroqResponse response = restClient.post()
					.uri(properties.url())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"model", properties.model(),
							"messages", messages(system, history, userMessage),
							"max_tokens", MAX_TOKENS,
							"temperature", TEMPERATURE,
							"reasoning_effort", "low"))
					.retrieve()
					.body(GroqResponse.class);
			String reply = replyText(response);
			if (reply.isBlank()) {
				log.warn("Groq returned an empty reply");
				throw new ChatUnavailableException();
			}
			return reply;
		} catch (ChatUnavailableException ex) {
			throw ex;
		} catch (RestClientResponseException ex) {
			log.warn("Groq call failed with status {}", ex.getStatusCode().value());
			throw new ChatUnavailableException();
		} catch (RestClientException | HttpMessageConversionException ex) {
			log.warn("Groq call failed ({})", ex.getClass().getSimpleName());
			throw new ChatUnavailableException();
		}
	}

	private static List<Map<String, String>> messages(String system, List<Turn> history, String userMessage) {
		List<Map<String, String>> messages = new ArrayList<>();
		messages.add(Map.of("role", "system", "content", system));
		if (history != null) {
			for (Turn turn : history) {
				messages.add(Map.of("role", turn.role(), "content", turn.content()));
			}
		}
		messages.add(Map.of("role", "user", "content", userMessage));
		return messages;
	}

	private static String replyText(GroqResponse response) {
		if (response == null || response.choices() == null || response.choices().isEmpty()) {
			return "";
		}
		GroqMessage message = response.choices().get(0).message();
		if (message == null || message.content() == null) {
			return "";
		}
		return message.content().trim();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	public record Turn(String role, String content) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GroqResponse(List<GroqChoice> choices) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GroqChoice(GroqMessage message) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GroqMessage(String content) {
	}
}
