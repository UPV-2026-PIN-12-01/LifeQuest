package com.lifequest.model.dto;

// A player matching what you typed in the "add friend" box, and where you stand with them:
// NONE, FRIEND, OUTGOING (you already asked them) or INCOMING (they asked you).
public record PlayerSuggestionDto(FriendSummaryDto player, String relation) {
}
