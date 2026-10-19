package com.lifequest.model.dto;

// Shape matches the home calendar cards: date is yyyy-MM-dd and times are HH:mm in the app time zone.
public record TaskDto(
		Long id,
		String title,
		String description,
		String icon,
		String color,
		String date,
		String startTime,
		String endTime,
		String status,
		String priority,
		long xp) {
}
