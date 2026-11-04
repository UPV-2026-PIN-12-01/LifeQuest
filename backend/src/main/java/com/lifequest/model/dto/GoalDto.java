package com.lifequest.model.dto;

import java.time.LocalDate;

public record GoalDto(
		Long id,
		String title,
		String description,
		LocalDate startDate,
		LocalDate endDate,
		boolean completed,
		Short rating,
		String status,
		long xpReward,
		long goldReward) {
}
