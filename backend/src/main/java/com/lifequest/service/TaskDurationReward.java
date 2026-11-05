package com.lifequest.service;

import java.time.Duration;

final class TaskDurationReward {
	private static final Duration BONUS_START = Duration.ofMinutes(30);
	private static final Duration BONUS_MAX = Duration.ofHours(3);
	private static final double MIN_MULTIPLIER = 1.1;
	private static final double MAX_MULTIPLIER = 2.0;

	private TaskDurationReward() {
	}

	static double multiplier(Duration duration) {
		if (duration.compareTo(BONUS_START) < 0) {
			return 1.0;
		}
		if (duration.compareTo(BONUS_MAX) >= 0) {
			return MAX_MULTIPLIER;
		}
		double progress = (double) (duration.toNanos() - BONUS_START.toNanos())
				/ (BONUS_MAX.toNanos() - BONUS_START.toNanos());
		return MIN_MULTIPLIER + progress * (MAX_MULTIPLIER - MIN_MULTIPLIER);
	}

	static long apply(long baseXp, Duration duration) {
		return Math.round(baseXp * multiplier(duration));
	}
}
