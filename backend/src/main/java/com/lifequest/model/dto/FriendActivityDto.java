package com.lifequest.model.dto;

// One entry of the friends activity feed: who completed which task.
public record FriendActivityDto(FriendSummaryDto player, FriendTaskDto task) {
}
