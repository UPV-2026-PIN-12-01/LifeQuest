package com.lifequest.model.entity;

import java.util.Arrays;
import java.util.Optional;

import com.lifequest.exception.InvalidRequestException;

public enum GoalRewardTier {
	BRONZE(1, 100, 50, 24),
	SILVER(2, 150, 75, 36),
	GOLD(3, 200, 100, 48),
	PLATINUM(4, 250, 125, 60),
	DIAMOND(5, 300, 150, 72);

	private final int level;
	private final long xp;
	private final long gold;
	private final int claimWaitHours;

	GoalRewardTier(int level, long xp, long gold, int claimWaitHours) {
		this.level = level;
		this.xp = xp;
		this.gold = gold;
		this.claimWaitHours = claimWaitHours;
	}

	public int level() {
		return level;
	}

	public long xp() {
		return xp;
	}

	public long gold() {
		return gold;
	}

	public int claimWaitHours() {
		return claimWaitHours;
	}

	public static GoalRewardTier fromLevel(int level) {
		return Arrays.stream(values())
				.filter(tier -> tier.level == level)
				.findFirst()
				.orElseThrow(() -> new InvalidRequestException("rewardTier must be between 1 and 5"));
	}

	public static Optional<GoalRewardTier> findByReward(long xp, long gold) {
		return Arrays.stream(values())
				.filter(tier -> tier.xp == xp && tier.gold == gold)
				.findFirst();
	}
}
