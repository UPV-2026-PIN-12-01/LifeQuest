package com.lifequest.model.dto;

public record DailyQuestDto(
		String id,
		String title,
		String icon,
		String color,
		long progress,
		long goal,
		long xp) {
}
