package com.lifequest.model.dto;

import java.time.LocalDateTime;

public record GoalRequestDto(
		String title,
		String description,
		LocalDateTime startDate,
		LocalDateTime endDate,
		int rewardTier) {
}
