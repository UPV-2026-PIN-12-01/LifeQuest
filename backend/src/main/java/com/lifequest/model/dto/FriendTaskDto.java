package com.lifequest.model.dto;

import com.lifequest.model.entity.ActivityType;

// A finished task as a friend sees it: no description, no times. date is yyyy-MM-dd; durationMinutes is end - start.
public record FriendTaskDto(Long id, String title, String icon, ActivityType activityType, String date,
		long durationMinutes, long xp) {
}
