package com.lifequest.model.dto;

import java.util.List;

public record FriendsOverviewDto(
		List<FriendSummaryDto> friends,
		List<FriendRequestDto> incoming,
		List<FriendRequestDto> outgoing) {
}
