package com.lifequest.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// Body of create and edit: date and times are in the app time zone, priority is low, medium or high.
public record TaskRequestDto(
		String title,
		String description,
		String icon,
		String color,
		LocalDate date,
		LocalTime startTime,
		LocalTime endTime,
		String priority) {
}
