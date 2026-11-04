package com.lifequest.model.dto;

import java.time.LocalDate;

public record GoalRequestDto(
		String title,
		String description,
		LocalDate startDate,
		LocalDate endDate,
		long xpReward,
		long goldReward) {
}
