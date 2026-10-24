package com.lifequest.model.dto;

// Shape matches the home calendar cards: date is yyyy-MM-dd and times are HH:mm in the app time zone.
// xp is the base reward until the task is completed, then what was granted. verification is none, checkin
// or focus; sessionStartedAt is the ISO check-in instant, null without a check-in.
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
		long xp,
		String verification,
		String sessionStartedAt,
		boolean sessionOpen,
		long focusedSeconds) {
}
