package com.lifequest.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.model.dto.DailyQuestDto;
import com.lifequest.model.dto.PlayerProfileDto;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.User;

// Read model for the home page: joins the user, their character and today's tasks.
@Service
public class PlayerService {

	static final int DAILY_TASKS_GOAL = 2;
	static final int STREAK_GOAL = 5;

	private final UserService userService;
	private final CharacterStatsService characterStatsService;
	private final TaskService taskService;

	public PlayerService(UserService userService, CharacterStatsService characterStatsService, TaskService taskService) {
		this.userService = userService;
		this.characterStatsService = characterStatsService;
		this.taskService = taskService;
	}

	@Transactional(readOnly = true)
	public PlayerProfileDto getProfile(Long userId) {
		User user = userService.getById(userId);
		CharacterStats stats = characterStatsService.getByUserId(userId);
		int level = stats.getLevel() == null ? 1 : stats.getLevel();
		return new PlayerProfileDto(
				user.getId(),
				user.getUsername(),
				stats.getPlayerName(),
				stats.getPlayerClass().displayName(),
				user.getProfileUrl(),
				level,
				stats.getXpCurrent() == null ? 0 : stats.getXpCurrent(),
				CharacterStatsService.xpForNextLevel(level),
				streakOf(user));
	}

	// Quests are not stored yet: progress is computed from today's tasks and the streak.
	@Transactional(readOnly = true)
	public List<DailyQuestDto> getDailyQuests(Long userId) {
		User user = userService.getById(userId);
		long completedToday = taskService.countCompletedToday(userId);
		return List.of(
				new DailyQuestDto("login", "Entra en LifeQuest", "🌅", "green", 1, 1, 10),
				new DailyQuestDto("tasks", "Completa " + DAILY_TASKS_GOAL + " tareas hoy", "📚", "purple",
						Math.min(completedToday, DAILY_TASKS_GOAL), DAILY_TASKS_GOAL, 30),
				new DailyQuestDto("streak", "Mantén la racha (" + STREAK_GOAL + " días)", "🔥", "orange",
						Math.min(streakOf(user), STREAK_GOAL), STREAK_GOAL, 25));
	}

	private static int streakOf(User user) {
		return user.getTaskStreak() == null ? 0 : user.getTaskStreak();
	}
}
