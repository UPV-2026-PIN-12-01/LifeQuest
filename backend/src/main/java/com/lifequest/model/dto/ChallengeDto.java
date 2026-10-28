package com.lifequest.model.dto;

import java.time.Instant;

public record ChallengeDto(
		Long id,
		Instant createdAt,
		String name,
		String description,
		String difficulty,
		boolean completed,
		Long taskId,
		Short rating,
		String visibility,
		String requirement,
		long xp,
		long gold,
		boolean rewardClaimed,
		TaskDto task) {
}
