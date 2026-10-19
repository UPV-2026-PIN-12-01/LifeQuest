package com.lifequest.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateUserRequest(String playerName, String userIcon, String email, String password) {
}
