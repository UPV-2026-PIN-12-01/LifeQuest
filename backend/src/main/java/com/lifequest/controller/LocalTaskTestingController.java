package com.lifequest.controller;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.TaskDto;
import com.lifequest.service.TaskCompletionService;
import com.lifequest.service.UserService;

@Profile("local")
@RestController
@RequestMapping("/api/players/me/tasks")
public class LocalTaskTestingController {

	private final TaskCompletionService taskCompletionService;
	private final UserService userService;

	public LocalTaskTestingController(TaskCompletionService taskCompletionService, UserService userService) {
		this.taskCompletionService = taskCompletionService;
		this.userService = userService;
	}

	@GetMapping("/testing-mode")
	public Map<String, Boolean> testingMode() {
		return Map.of("enabled", true);
	}

	@PostMapping("/{taskId}/complete-instant")
	public TaskDto completeInstantly(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
		return taskCompletionService.completeInstantlyForTesting(
				userService.getIdByAuthSubject(jwt.getSubject()), taskId);
	}
}
