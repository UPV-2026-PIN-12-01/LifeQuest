package com.lifequest.service.tool;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.ChallengeRequestDto;
import com.lifequest.service.TaskService;

import tools.jackson.databind.json.JsonMapper;

@Component
@Order(2)
public class ProposeChallengeTool implements ChatTool {

	private static final int MAX_NAME = 120;
	private static final int MAX_WHY = 300;
	private static final Set<String> DIFFICULTIES = Set.of("EASY", "NORMAL", "HARD", "VERY HARD", "LEGENDARY FEAT");

	private final JsonMapper jsonMapper;
	private final TaskService taskService;

	public ProposeChallengeTool(JsonMapper jsonMapper, TaskService taskService) {
		this.jsonMapper = jsonMapper;
		this.taskService = taskService;
	}

	@Override
	public String name() {
		return "propose_challenge";
	}

	@Override
	public Map<String, Object> spec() {
		return Map.of(
				"type", "function",
				"function", Map.of(
						"name", name(),
						"description", "Propose a challenge on a task this user already has. Does not create it. taskId is the id from the week task list. Use the exact name they asked for.",
						"parameters", Map.of(
								"type", "object",
								"properties", Map.of(
										"name", Map.of("type", "string", "description", "Challenge name, 1 to 120 characters."),
										"difficulty", Map.of("type", "string", "enum", List.of("EASY", "NORMAL", "HARD", "VERY HARD", "LEGENDARY FEAT")),
										"taskId", Map.of("type", "integer", "description", "Id of one of this user's tasks."),
										"why", Map.of("type", "string", "description", "One sentence for the proposal card."),
										"description", Map.of("type", "string", "description", "Defaults to empty when omitted."),
										"requirement", Map.of("type", "string", "description", "Defaults to empty when omitted.")),
								"required", List.of("name", "difficulty", "taskId", "why"))));
	}

	@Override
	public Outcome call(Long userId, String arguments) {
		ToolArguments args = ToolArguments.parse(jsonMapper, arguments);
		if (args.error() != null) {
			return Outcome.error(args.error());
		}
		String name = args.text("name");
		if (name.isEmpty() || name.length() > MAX_NAME) {
			return Outcome.error("name must be 1 to " + MAX_NAME + " characters");
		}
		String why = args.text("why");
		if (why.isEmpty() || why.length() > MAX_WHY) {
			return Outcome.error("why must be one sentence of at most " + MAX_WHY + " characters");
		}
		String difficulty = args.text("difficulty").toUpperCase(Locale.ROOT);
		if (!DIFFICULTIES.contains(difficulty)) {
			return Outcome.error("difficulty must be one of EASY, NORMAL, HARD, VERY HARD, LEGENDARY FEAT");
		}
		if (!args.present("taskId")) {
			return Outcome.error("taskId is required");
		}
		Long taskId = args.longValue("taskId");
		if (taskId == null) {
			return Outcome.error("taskId must be a number");
		}
		try {
			taskService.findOwned(userId, taskId);
		} catch (ResourceNotFoundException ex) {
			return Outcome.error("taskId is not one of this user's tasks");
		}
		ChallengeRequestDto request = new ChallengeRequestDto(
				name, args.text("description"), difficulty, taskId, args.text("requirement"));
		return Outcome.pending(new Draft("challenge", name, difficulty, why, jsonMapper.writeValueAsString(request)));
	}
}
