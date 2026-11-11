package com.lifequest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddFriendRequestDto(@NotBlank @Size(max = 80) String playerName) {
}
