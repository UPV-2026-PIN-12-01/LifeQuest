package com.lifequest.model.dto;

import jakarta.validation.constraints.Min;

public record CharacterStatsDto(
		@Min(0) long hp,
		@Min(0) int strength,
		@Min(0) int speed,
		@Min(0) int stamina,
		@Min(0) int wisdom) {
}
