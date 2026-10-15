package com.lifequest.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.DailyQuestDto;
import com.lifequest.model.dto.PlayerProfileDto;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.service.PlayerService;
import com.lifequest.service.TaskService;
import com.lifequest.service.UserService;

// Home page data for the logged-in user: the Supabase JWT subject is users.inner_id.
@RestController
@RequestMapping("/api/players/me")
public class PlayerController {

	private final PlayerService playerService;
	private final TaskService taskService;
	private final UserService userService;

	public PlayerController(PlayerService playerService, TaskService taskService, UserService userService) {
		this.playerService = playerService;
		this.taskService = taskService;
		this.userService = userService;
	}

	@GetMapping
	public PlayerProfileDto profile(@AuthenticationPrincipal Jwt jwt) {
		return playerService.getProfile(userService.getIdByAuthSubject(jwt.getSubject()));
	}

	@GetMapping("/tasks")
	public List<TaskDto> tasks(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return taskService.findForUser(userService.getIdByAuthSubject(jwt.getSubject()), from, to);
	}

	@GetMapping("/daily-quests")
	public List<DailyQuestDto> dailyQuests(@AuthenticationPrincipal Jwt jwt) {
		return playerService.getDailyQuests(userService.getIdByAuthSubject(jwt.getSubject()));
	}
}
