package com.lifequest.model.dto;

import java.util.List;

public record MarketItemDto(
		String id,
		String catalogId,
		String name,
		String icon,
		String category,
		String rarity,
		int level,
		int maxLevel,
		int baseUpgradePrice,
		List<String> stats,
		Integer heal,
		Integer staminaRestore,
		int buyPrice,
		boolean equipped,
		int quantity) {
}
