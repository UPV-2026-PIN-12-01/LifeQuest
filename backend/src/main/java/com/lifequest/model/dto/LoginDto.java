package com.lifequest.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginDto(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("refresh_token") String refreshToken,
		@JsonProperty("expires_in") long expiresIn,
		@JsonProperty("token_type") String tokenType) {
}
