package com.lifequest.model.dto;

public record GoalQuotaDto(
		long createdThisWeek,
		int weeklyLimit) {
}
