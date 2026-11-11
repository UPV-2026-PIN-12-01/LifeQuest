package com.lifequest.model.dto;

import java.time.LocalDateTime;
import java.time.Instant;

public record GoalDto(
		Long id,
		String title,
		String description,
		Instant createdAt,
		LocalDateTime startDate,
		LocalDateTime endDate,
		boolean completed,
		Short rating,
		String status,
		int rewardTier,
		long xpReward,
		long goldReward,
		Instant rewardClaimableAt,
		boolean rewardClaimed) {
}
