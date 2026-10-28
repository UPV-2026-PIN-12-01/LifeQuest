package com.lifequest.model.dto;

public record ChallengeRequestDto(
		String name,
		String description,
		String difficulty,
		Long taskId,
		String requirement) {
}
