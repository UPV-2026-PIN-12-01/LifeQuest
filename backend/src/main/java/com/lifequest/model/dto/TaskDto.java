package com.lifequest.model.dto;
import java.util.List;
import com.lifequest.model.entity.ActivityType;

// Shape matches the home calendar cards: date is yyyy-MM-dd and times are HH:mm in the app time zone.
// xp is the duration-adjusted estimate until completion, then what was granted. verification is none,
// checkin or focus; sessionStartedAt is the ISO check-in instant, null without a check-in.
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
		long gold,
		ActivityType activityType,
		String verification,
		String sessionStartedAt,
		boolean sessionOpen,
		long focusedSeconds,
		List<String> newlyUnlockedAchievements) {
	public TaskDto(Long id, String title, String description, String icon, String color, String date,
			String startTime, String endTime, String status, String priority, long xp, long gold,
			ActivityType activityType, String verification, String sessionStartedAt, boolean sessionOpen,
			long focusedSeconds) {
		this(id, title, description, icon, color, date, startTime, endTime, status, priority, xp, gold,
				activityType, verification, sessionStartedAt, sessionOpen, focusedSeconds, List.of());
	}
}
