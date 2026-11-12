package com.lifequest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.MarketDto;
import com.lifequest.model.dto.MarketItemDto;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.PlayerInventoryItem;
import com.lifequest.model.entity.ToolIndexItem;
import com.lifequest.repository.CharacterStatsRepository;
import com.lifequest.repository.PlayerInventoryRepository;
import com.lifequest.repository.ToolIndexRepository;

@Service
public class MarketService {

	private static final Map<String, String> STAT_LABELS = Map.of(
			"hp", "Vida",
			"strength", "Fuerza",
			"knowledge", "Conocimiento",
			"speed", "Velocidad",
			"stamina", "Estamina");
	private final CharacterStatsRepository characterStatsRepository;
	private final PlayerInventoryRepository inventoryRepository;
	private final ToolIndexRepository toolIndexRepository;

	public MarketService(
			CharacterStatsRepository characterStatsRepository,
			PlayerInventoryRepository inventoryRepository,
			ToolIndexRepository toolIndexRepository) {
		this.characterStatsRepository = characterStatsRepository;
		this.inventoryRepository = inventoryRepository;
		this.toolIndexRepository = toolIndexRepository;
	}

	@Transactional(readOnly = true)
	public MarketDto getMarket(Long userId) {
		CharacterStats character = getCharacter(userId);
		List<MarketItemDto> inventory = inventoryRepository
				.findByCharacterIdOrderById(character.getId()).stream()
				.map(this::toInventoryDto)
				.toList();
		List<MarketItemDto> shopItems = toolIndexRepository
				.findByCatalogIdIsNotNullOrderByIdAsc().stream()
				.map(this::toShopDto)
				.toList();
		return new MarketDto(shopItems, inventory, getObjectStats(character.getId()), character.getGold());
	}

	@Transactional
	public MarketDto buy(Long userId, String catalogId) {
		CharacterStats character = lockCharacter(userId);
		ToolIndexItem item = getCatalogItem(catalogId);
		if (character.getLevel() < item.getBaseLevel()) {
			throw new InvalidRequestException(
					"Nivel insuficiente: necesitas nivel " + item.getBaseLevel() + ".");
		}
		if (character.getGold() < item.getPurchasePrice()) {
			throw new InvalidRequestException("Oro insuficiente.");
		}

		character.setGold(character.getGold() - item.getPurchasePrice());
		if (isPotion(item)) {
			PlayerInventoryItem potion = inventoryRepository
					.findFirstByCharacterIdAndCatalogIdOrderById(character.getId(), item.getCatalogId())
					.orElse(null);
			if (potion != null) {
				try {
					potion.setQuantity(Math.addExact(potion.getQuantity(), 1));
				} catch (ArithmeticException ex) {
					throw new InvalidRequestException("La cantidad máxima es 2.147.483.647.");
				}
			} else {
				inventoryRepository.save(newInventoryItem(character, item));
			}
		} else {
			inventoryRepository.save(newInventoryItem(character, item));
		}
		characterStatsRepository.save(character);
		return getMarket(userId);
	}

	@Transactional
	public MarketDto setEquipped(Long userId, Long inventoryId, boolean equipped) {
		CharacterStats character = lockCharacter(userId);
		PlayerInventoryItem item = getInventoryItem(inventoryId, character.getId());
		if (equipped && isPotion(item)) {
			throw new InvalidRequestException("Las pociones no se pueden equipar.");
		}
		if (equipped) {
			inventoryRepository.unequipOtherItemsInSlot(
					character.getId(), item.getSlot(), item.getId());
		}
		item.setEquipped(equipped);
		inventoryRepository.save(item);
		return getMarket(userId);
	}

	@Transactional
	public MarketDto sell(Long userId, Long inventoryId) {
		CharacterStats character = lockCharacter(userId);
		PlayerInventoryItem item = getInventoryItem(inventoryId, character.getId());
		ToolIndexItem catalogItem = getCatalogItem(item.getCatalogId());
		long salePrice = catalogItem.getPurchasePrice() * 7L / 10;
		try {
			character.setGold(Math.addExact(character.getGold(), salePrice));
		} catch (ArithmeticException ex) {
			throw new InvalidRequestException("El saldo supera el límite permitido.");
		}
		if (item.getQuantity() > 1) {
			item.setQuantity(item.getQuantity() - 1);
			inventoryRepository.save(item);
		} else {
			inventoryRepository.delete(item);
		}
		characterStatsRepository.save(character);
		return getMarket(userId);
	}

	@Transactional
	public MarketDto upgrade(Long userId, Long inventoryId) {
		CharacterStats character = lockCharacter(userId);
		PlayerInventoryItem item = getInventoryItem(inventoryId, character.getId());
		if (isPotion(item)) {
			throw new InvalidRequestException("Las pociones no se pueden mejorar.");
		}
		ToolIndexItem catalogItem = getCatalogItem(item.getCatalogId());
		if (item.getLevel() >= catalogItem.getMaxLevel()) {
			throw new InvalidRequestException("El objeto ya ha alcanzado su nivel máximo.");
		}
		long cost = item.getLevel() * (long) catalogItem.getBaseUpgradePrice();
		if (character.getGold() < cost) {
			throw new InvalidRequestException("No tienes suficiente oro.");
		}
		character.setGold(character.getGold() - cost);
		item.setLevel(item.getLevel() + 1);
		item.setStats(increaseStats(item.getStats(), catalogItem.getUpgradeStatBuffs()));
		characterStatsRepository.save(character);
		inventoryRepository.save(item);
		return getMarket(userId);
	}

	private CharacterStats getCharacter(Long userId) {
		return characterStatsRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado."));
	}

	private CharacterStats lockCharacter(Long userId) {
		return characterStatsRepository.findByUserIdForUpdate(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Personaje no encontrado."));
	}

	private PlayerInventoryItem getInventoryItem(Long inventoryId, Long characterId) {
		return inventoryRepository.findByIdAndCharacterId(inventoryId, characterId)
				.orElseThrow(() -> new ResourceNotFoundException("Objeto no encontrado."));
	}

	private ToolIndexItem getCatalogItem(String catalogId) {
		return toolIndexRepository.findByCatalogId(catalogId)
				.orElseThrow(() -> new ResourceNotFoundException("Objeto de mercado no encontrado."));
	}

	private PlayerInventoryItem newInventoryItem(CharacterStats character, ToolIndexItem item) {
		PlayerInventoryItem inventoryItem = new PlayerInventoryItem();
		inventoryItem.setCharacterId(character.getId());
		inventoryItem.setCatalogId(item.getCatalogId());
		inventoryItem.setSlot(item.getCategory());
		inventoryItem.setLevel(item.getBaseLevel());
		inventoryItem.setQuantity(1);
		inventoryItem.setEquipped(false);
		inventoryItem.setStats(new LinkedHashMap<>(item.getBaseStatsBuff()));
		return inventoryItem;
	}

	private boolean isPotion(PlayerInventoryItem item) {
		return "Poción".equals(item.getSlot());
	}

	private boolean isPotion(ToolIndexItem item) {
		return "Poción".equals(item.getCategory());
	}

	private MarketItemDto toShopDto(ToolIndexItem item) {
		return new MarketItemDto(
				item.getCatalogId(),
				item.getCatalogId(),
				item.getName(),
				item.getIcon(),
				item.getCategory(),
				item.getRarity(),
				item.getBaseLevel(),
				item.getMaxLevel(),
				item.getBaseUpgradePrice(),
				getDisplayStats(item.getBaseStatsBuff(), item),
				item.getHeal(),
				item.getStaminaRestore(),
				item.getPurchasePrice(),
				false,
				1);
	}

	private MarketItemDto toInventoryDto(PlayerInventoryItem item) {
		ToolIndexItem catalogItem = getCatalogItem(item.getCatalogId());
		return new MarketItemDto(
				item.getId().toString(),
				item.getCatalogId(),
				catalogItem.getName(),
				catalogItem.getIcon(),
				catalogItem.getCategory(),
				catalogItem.getRarity(),
				item.getLevel(),
				catalogItem.getMaxLevel(),
				catalogItem.getBaseUpgradePrice(),
				getDisplayStats(item.getStats(), catalogItem),
				catalogItem.getHeal(),
				catalogItem.getStaminaRestore(),
				catalogItem.getPurchasePrice(),
				Boolean.TRUE.equals(item.getEquipped()),
				item.getQuantity());
	}

	private List<String> getDisplayStats(Map<String, Integer> stats, ToolIndexItem item) {
		List<String> displayStats = stats.entrySet().stream()
				.map(entry -> STAT_LABELS.getOrDefault(entry.getKey(), entry.getKey())
						+ " +" + entry.getValue())
				.collect(Collectors.toCollection(ArrayList::new));
		if (item.getHeal() != null) {
			displayStats.add("Recupera " + item.getHeal() + " de vida");
		}
		if (item.getStaminaRestore() != null) {
			displayStats.add("Recupera " + item.getStaminaRestore() + " de estamina");
		}
		return displayStats;
	}

	private Map<String, Integer> increaseStats(Map<String, Integer> currentStats, Integer increment) {
		Map<String, Integer> updatedStats = new HashMap<>(currentStats);
		try {
			updatedStats.replaceAll((stat, value) -> Math.addExact(value, increment));
		} catch (ArithmeticException ex) {
			throw new InvalidRequestException("Las estadísticas superan el límite permitido.");
		}
		return updatedStats;
	}

	private Map<String, Long> getObjectStats(Long characterId) {
		Map<String, Long> stats = new HashMap<>();
		for (Object[] row : inventoryRepository.sumStatsByCharacterId(characterId)) {
			stats.put((String) row[0], ((Number) row[1]).longValue());
		}
		return stats;
	}
}
