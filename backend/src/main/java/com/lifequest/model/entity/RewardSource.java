package com.lifequest.model.entity;

public enum RewardSource {
	TASK("task"),
	CHALLENGE("challenge"),
	OBJECTIVE("objective"),
	ARCHIVEMENT("archivement"),
	BOSS_BATTLE("boss_battle"),
	MONSTER_BATTLE("monster_battle"),
	PLAYER_BATTLE("player_battle");

	private final String databaseValue;

	RewardSource(String databaseValue) {
		this.databaseValue = databaseValue;
	}

	public String databaseValue() {
		return databaseValue;
	}

	public static RewardSource fromDatabaseValue(String value) {
		for (RewardSource source : values()) {
			if (source.databaseValue.equals(value)) {
				return source;
			}
		}
		throw new IllegalArgumentException("Unknown reward source: " + value);
	}
}
