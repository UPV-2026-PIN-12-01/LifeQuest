package com.lifequest.model.dto;

// What any friend (or someone who sent you a request) can see of a player in a list.
public record FriendSummaryDto(
		Long userId,
		String playerName,
		String playerClass,
		String userIcon,
		String profileUrl,
		int level) {
}
