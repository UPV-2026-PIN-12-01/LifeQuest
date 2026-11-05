package com.lifequest.model.dto;

public record UserAccountDto(
		Long id,
		String username,
		String email,
		String userIcon,
		String playerName,
		String playerClass,
		String profileUrl,
		int streakDays,
		int dayStreak,
		int taskStreak,
		float xpAccBoost) {
}
