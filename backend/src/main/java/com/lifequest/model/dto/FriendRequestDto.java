package com.lifequest.model.dto;

// incoming: the other player asked you; otherwise you asked them and it is still pending.
public record FriendRequestDto(Long id, FriendSummaryDto player, boolean incoming, String createdAt) {
}
