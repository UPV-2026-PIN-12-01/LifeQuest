package com.lifequest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.dto.TaskRequestDto;
import com.lifequest.service.TaskCompletionService;
import com.lifequest.service.TaskService;
import com.lifequest.service.UserService;

// Create, edit, delete and confirm the logged-in user's tasks; listing lives in PlayerController.
@RestController
@RequestMapping("/api/players/me/tasks")
public class TaskController {

	private final TaskService taskService;
	private final TaskCompletionService taskCompletionService;
	private final UserService userService;

	public TaskController(TaskService taskService, TaskCompletionService taskCompletionService, UserService userService) {
		this.taskService = taskService;
		this.taskCompletionService = taskCompletionService;
		this.userService = userService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TaskDto create(@AuthenticationPrincipal Jwt jwt, @RequestBody TaskRequestDto request) {
		return taskService.create(userService.getIdByAuthSubject(jwt.getSubject()), request);
	}

	@PutMapping("/{taskId}")
	public TaskDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId, @RequestBody TaskRequestDto request) {
		return taskService.update(userService.getIdByAuthSubject(jwt.getSubject()), taskId, request);
	}

	@DeleteMapping("/{taskId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
		taskService.delete(userService.getIdByAuthSubject(jwt.getSubject()), taskId);
	}

	@PostMapping("/{taskId}/start")
	public TaskDto start(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
		return taskCompletionService.start(userService.getIdByAuthSubject(jwt.getSubject()), taskId);
	}

	@PostMapping("/{taskId}/heartbeat")
	public TaskDto heartbeat(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
		return taskCompletionService.heartbeat(userService.getIdByAuthSubject(jwt.getSubject()), taskId);
	}

	@PostMapping("/{taskId}/complete")
	public TaskDto complete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
		return taskCompletionService.complete(userService.getIdByAuthSubject(jwt.getSubject()), taskId);
	}

	@PostMapping("/{taskId}/skip")
	public TaskDto skip(@AuthenticationPrincipal Jwt jwt, @PathVariable Long taskId) {
		return taskCompletionService.skip(userService.getIdByAuthSubject(jwt.getSubject()), taskId);
	}

}
