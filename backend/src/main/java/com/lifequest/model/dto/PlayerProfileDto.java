package com.lifequest.model.dto;

public record PlayerProfileDto(
		Long userId,
		String username,
		String playerName,
		String playerClass,
		int level,
		long xp,
		long xpForNextLevel,
		int streakDays) {
}
