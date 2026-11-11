package com.lifequest.service.tool;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Reads one tool-call arguments object. A bad payload is an error string, not a stored proposal.
final class ToolArguments {

	private final JsonNode node;
	private final String error;

	private ToolArguments(JsonNode node, String error) {
		this.node = node;
		this.error = error;
	}

	static ToolArguments parse(JsonMapper jsonMapper, String arguments) {
		if (arguments == null || arguments.isBlank()) {
			return new ToolArguments(jsonMapper.createObjectNode(), null);
		}
		try {
			JsonNode node = jsonMapper.readTree(arguments);
			if (node == null || !node.isObject()) {
				return new ToolArguments(null, "arguments must be a JSON object");
			}
			return new ToolArguments(node, null);
		} catch (RuntimeException ex) {
			return new ToolArguments(null, "arguments must be a JSON object");
		}
	}

	String error() {
		return error;
	}

	String text(String field) {
		return text(field, null);
	}

	String text(String field, String alias) {
		String value = raw(field);
		if (!value.isEmpty() || alias == null) {
			return value;
		}
		return raw(alias);
	}

	Long longValue(String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		if (value.isNumber()) {
			return value.longValue();
		}
		String text = value.asString();
		if (text == null || text.isBlank()) {
			return null;
		}
		try {
			return Long.valueOf(text.trim());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	boolean present(String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return false;
		}
		if (value.isNumber()) {
			return true;
		}
		String text = value.asString();
		return text != null && !text.isBlank();
	}

	private String raw(String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return "";
		}
		String text = value.asString();
		return text == null ? "" : text.trim();
	}
}
