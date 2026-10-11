package com.lifequest.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.DailyQuestDto;
import com.lifequest.model.dto.PlayerProfileDto;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.service.PlayerService;
import com.lifequest.service.TaskService;

// Home page data. TODO: replace {userId} with the user from the Supabase JWT once login exists.
@RestController
@RequestMapping("/api/players/{userId}")
public class PlayerController {

	private final PlayerService playerService;
	private final TaskService taskService;

	public PlayerController(PlayerService playerService, TaskService taskService) {
		this.playerService = playerService;
		this.taskService = taskService;
	}

	@GetMapping
	public PlayerProfileDto profile(@PathVariable Long userId) {
		return playerService.getProfile(userId);
	}

	@GetMapping("/tasks")
	public List<TaskDto> tasks(
			@PathVariable Long userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return taskService.findForUser(userId, from, to);
	}

	@GetMapping("/daily-quests")
	public List<DailyQuestDto> dailyQuests(@PathVariable Long userId) {
		return playerService.getDailyQuests(userId);
	}
}
