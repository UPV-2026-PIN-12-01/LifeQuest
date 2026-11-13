package com.lifequest.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

import com.lifequest.config.GroqProperties;
import com.lifequest.exception.ChatUnavailableException;
import com.lifequest.exception.EmptyGroqReplyException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GroqClient {

	private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
	// Reasoning tokens count toward this cap. A past start used the whole 600 and came back empty.
	private static final int MAX_TOKENS = 2048;
	// One round per proposal, plus room for the limit sentence. Keep this above ChatService.MAX_PROPOSALS.
	private static final int MAX_ROUNDS = 12;
	private static final double TEMPERATURE = 0.3;

	private final RestClient restClient;
	private final GroqProperties properties;
	private final JsonMapper jsonMapper;

	public GroqClient(
			@Qualifier("groqRestClient") RestClient restClient,
			GroqProperties properties,
			JsonMapper jsonMapper) {
		this.restClient = restClient;
		this.properties = properties;
		this.jsonMapper = jsonMapper;
	}

	// history is this thread only, oldest first, and does not include userMessage.
	// At most MAX_ROUNDS completions. Tool results stay in this call; nothing is stored here.
	public String reply(String system, List<Turn> history, String userMessage, List<Map<String, Object>> tools, ToolCaller caller) {
		if (isBlank(properties.apiKey()) || isBlank(properties.model()) || isBlank(properties.url())) {
			log.warn("Groq is not configured");
			throw new ChatUnavailableException();
		}
		List<Map<String, Object>> messages = messages(system, history, userMessage);
		boolean sawTool = false;
		int emptyNudges = 0;
		for (int round = 0; round < MAX_ROUNDS; round++) {
			JsonNode response = complete(messages, tools);
			JsonNode choice = choice(response);
			JsonNode message = choice.path("message");
			if (message.isMissingNode() || message.isNull()) {
				message = response.path("message");
			}
			List<ToolCall> calls = toolCalls(message);
			boolean recovered = false;
			if (calls.isEmpty()) {
				String reply = text(message.path("content"));
				if (!reply.isBlank()) {
					return reply;
				}
				ToolCall fromReasoning = recover(reasoning(message));
				if (fromReasoning != null) {
					calls = List.of(fromReasoning);
					recovered = true;
				} else if (emptyNudges >= 1) {
					log.warn("Groq returned an empty reply (finish_reason={})", finish(choice));
					throw new EmptyGroqReplyException();
				} else {
					emptyNudges++;
					log.warn("Groq returned an empty reply (finish_reason={}); asking once more", finish(choice));
					messages.add(Map.of("role", "user", "content", nudge(sawTool)));
					continue;
				}
			}
			sawTool = true;
			messages.add(recovered ? recoveredTurn(calls.get(0)) : assistantMessage(message));
			for (ToolCall call : calls) {
				String result = caller == null ? "Tools are not available" : caller.call(call.name(), call.arguments());
				messages.add(Map.of(
						"role", "tool",
						"tool_call_id", call.id(),
						"content", result == null ? "" : result));
			}
		}
		log.warn("Groq did not finish within {} rounds", MAX_ROUNDS);
		throw new ChatUnavailableException();
	}

	private JsonNode complete(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("model", properties.model());
		body.put("messages", messages);
		body.put("max_tokens", MAX_TOKENS);
		body.put("temperature", TEMPERATURE);
		body.put("reasoning_effort", "low");
		if (tools != null && !tools.isEmpty()) {
			body.put("tools", tools);
		}
		try {
			JsonNode response = restClient.post()
					.uri(properties.url())
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(JsonNode.class);
			if (response == null) {
				log.warn("Groq returned an empty body");
				throw new ChatUnavailableException();
			}
			return response;
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

	private Map<String, Object> assistantMessage(JsonNode message) {
		Map<String, Object> assistant = jsonMapper.convertValue(message, Map.class);
		assistant.put("role", "assistant");
		return assistant;
	}

	private static JsonNode choice(JsonNode response) {
		JsonNode choices = response.path("choices");
		if (!choices.isArray() || choices.isEmpty()) {
			return response;
		}
		return choices.get(0);
	}

	private static String finish(JsonNode choice) {
		String value = choice.path("finish_reason").asString("");
		return value.isBlank() ? "unknown" : value;
	}

	private static List<ToolCall> toolCalls(JsonNode message) {
		JsonNode calls = message.path("tool_calls");
		if (!calls.isArray() || calls.isEmpty()) {
			return List.of();
		}
		List<ToolCall> parsed = new ArrayList<>();
		int index = 0;
		for (JsonNode call : calls) {
			String id = call.path("id").asString("call-" + index);
			String name = call.path("function").path("name").asString("");
			parsed.add(new ToolCall(id, name, arguments(call.path("function").path("arguments"))));
			index++;
		}
		return parsed;
	}

	// gpt-oss sometimes leaves the function call in reasoning and sends no tool_calls.
	private static ToolCall recover(String reasoning) {
		if (reasoning == null || reasoning.isBlank()) {
			return null;
		}
		for (String name : List.of("propose_task", "propose_challenge")) {
			int at = reasoning.indexOf(name);
			if (at < 0) {
				continue;
			}
			int brace = reasoning.indexOf('{', at);
			if (brace < 0 || brace - at > 200) {
				continue;
			}
			String json = balanced(reasoning, brace);
			if (json == null) {
				continue;
			}
			return new ToolCall("recovered-" + name, name, json);
		}
		return null;
	}

	private static String balanced(String text, int open) {
		int depth = 0;
		boolean inString = false;
		boolean escape = false;
		for (int i = open; i < text.length(); i++) {
			char c = text.charAt(i);
			if (inString) {
				if (escape) {
					escape = false;
				} else if (c == '\\') {
					escape = true;
				} else if (c == '"') {
					inString = false;
				}
				continue;
			}
			if (c == '"') {
				inString = true;
			} else if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					return text.substring(open, i + 1);
				}
			}
		}
		return null;
	}

	private static String reasoning(JsonNode message) {
		String parsed = text(message.path("reasoning"));
		if (!parsed.isBlank()) {
			return parsed;
		}
		return text(message.path("reasoning_content"));
	}

	private static String nudge(boolean sawTool) {
		if (sawTool) {
			return "Your last reply was empty. If a proposal is already pending, answer in Spanish and tell the user to press Crear. Do not call tools again.";
		}
		return "Your last reply was empty. If the user asked for a task or a challenge, call the function now. If the start time already passed, use a later time today or the next day. Then answer in Spanish.";
	}

	private static Map<String, Object> recoveredTurn(ToolCall call) {
		Map<String, Object> assistant = new LinkedHashMap<>();
		assistant.put("role", "assistant");
		assistant.put("content", "");
		assistant.put("tool_calls", List.of(Map.of(
				"id", call.id(),
				"type", "function",
				"function", Map.of("name", call.name(), "arguments", call.arguments()))));
		return assistant;
	}

	private static String arguments(JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return "";
		}
		if (node.isString()) {
			return node.asString("");
		}
		return node.toString();
	}

	private static String text(JsonNode content) {
		if (content == null || content.isMissingNode() || content.isNull()) {
			return "";
		}
		if (content.isString()) {
			return content.asString("").trim();
		}
		if (!content.isArray()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (JsonNode part : content) {
			if (part.isString()) {
				builder.append(part.asString(""));
			} else {
				builder.append(part.path("text").asString(""));
			}
		}
		return builder.toString().trim();
	}

	private static List<Map<String, Object>> messages(String system, List<Turn> history, String userMessage) {
		List<Map<String, Object>> messages = new ArrayList<>();
		messages.add(Map.of("role", "system", "content", system));
		if (history != null) {
			for (Turn turn : history) {
				messages.add(Map.of("role", turn.role(), "content", turn.content()));
			}
		}
		messages.add(Map.of("role", "user", "content", userMessage));
		return messages;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	@FunctionalInterface
	public interface ToolCaller {
		String call(String name, String arguments);
	}

	public record Turn(String role, String content) {
	}

	private record ToolCall(String id, String name, String arguments) {
	}
}
