package com.lifequest.model.dto;

import java.util.List;

// A friend's character: stats and the tasks they completed recently. Gold and goals stay private.
public record FriendProfileDto(
		FriendSummaryDto player,
		long hp,
		int strength,
		int speed,
		int stamina,
		int wisdom,
		long xp,
		long xpForNextLevel,
		int dayStreak,
		int taskStreak,
		List<FriendTaskDto> completedTasks) {
}
