package com.lifequest.model.dto;

public record PlayerProfileDto(
		Long userId,
		String username,
		String playerName,
		String playerClass,
		String profileUrl,
		int level,
		long xp,
		long xpForNextLevel,
		int streakDays) {
}
