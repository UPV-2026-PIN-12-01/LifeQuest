package com.lifequest.model.dto;

public record PlayerProfileDto(
		Long userId,
		String username,
		String playerName,
		String playerClass,
		String profileUrl,
		String userIcon,
		int level,
		long xp,
		long xpForNextLevel,
		long gold,
		int streakDays,
		int dayStreak,
		int taskStreak,
		float xpAccBoost) {
}
