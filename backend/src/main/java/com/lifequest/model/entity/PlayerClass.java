package com.lifequest.model.entity;

public enum PlayerClass {
	knight,
	archer,
	magician;

	public static PlayerClass fromInput(String raw) {
		if (raw == null || raw.isBlank()) {
			return knight;
		}
		return switch (raw.trim().toLowerCase()) {
			case "guerrero", "knight" -> knight;
			case "arquero", "archer" -> archer;
			case "mago", "magician" -> magician;
			default -> throw new IllegalArgumentException("Unknown player class: " + raw);
		};
	}

	public String defaultIcon() {
		return switch (this) {
			case knight -> "⚔️";
			case archer -> "🏹";
			case magician -> "🪄";
		};
	}
}
