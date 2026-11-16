package com.lifequest.model.dto;

import java.time.Instant;

public record AchievementDto(
		Long id,
		String name,
		String description,
		String icon,
		long progress,
		long target,
		String unit,
		long xpReward,
		long goldReward,
		boolean unlocked,
		Instant unlockedAt,
		boolean rewardClaimed) {
}
