package com.lifequest.model.dto;

import java.util.List;
import java.util.Map;

public record MarketDto(
		List<MarketItemDto> shopItems,
		List<MarketItemDto> inventory,
		Map<String, Long> objectStats,
		long gold) {
}
