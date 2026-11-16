package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.PlayerInventoryItem;
import com.lifequest.model.entity.ToolIndexItem;
import com.lifequest.repository.CharacterStatsRepository;
import com.lifequest.repository.PlayerInventoryRepository;
import com.lifequest.repository.ToolIndexRepository;

class MarketServiceTest {

	private final CharacterStatsRepository characterStatsRepository = org.mockito.Mockito.mock(CharacterStatsRepository.class);
	private final PlayerInventoryRepository inventoryRepository = org.mockito.Mockito.mock(PlayerInventoryRepository.class);
	private final ToolIndexRepository toolIndexRepository = org.mockito.Mockito.mock(ToolIndexRepository.class);
	private final MarketService marketService = new MarketService(
			characterStatsRepository, inventoryRepository, toolIndexRepository);
	private final CharacterStats character = new CharacterStats();
	private final ToolIndexItem sword = catalogItem("sword", "Espada del Alba", "Arma", 260, 3,
			Map.of("strength", 8, "speed", 2));
	private final ToolIndexItem armor = catalogItem("armor", "Coraza del Guardián", "Armadura", 340, 4,
			Map.of("hp", 18, "stamina", 5));
	private final ToolIndexItem bow = catalogItem("bow", "Arco del Rastreador", "Arma", 480, 5,
			Map.of("strength", 11, "speed", 6));

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(character, "id", 70L);
		character.setUserId(7L);
		character.setPlayerName("test-can-delete-later");
		character.setGold(500L);
		character.setLevel(3);
		when(characterStatsRepository.findByUserIdForUpdate(7L)).thenReturn(Optional.of(character));
		when(characterStatsRepository.findByUserId(7L)).thenReturn(Optional.of(character));
		when(characterStatsRepository.save(character)).thenReturn(character);
		when(inventoryRepository.findByCharacterIdOrderById(70L)).thenReturn(List.of());
		when(inventoryRepository.sumEquippedStatsByCharacterId(70L)).thenReturn(List.of());
		when(toolIndexRepository.findByCatalogIdIsNotNullOrderByIdAsc()).thenReturn(List.of(sword, bow, armor));
		when(toolIndexRepository.findByCatalogId("sword")).thenReturn(Optional.of(sword));
		when(toolIndexRepository.findByCatalogId("armor")).thenReturn(Optional.of(armor));
		when(toolIndexRepository.findByCatalogId("bow")).thenReturn(Optional.of(bow));
	}

	@Test
	void newPlayerStartsWithAnEmptyInventoryAndCanSeeTheCatalog() {
		var market = marketService.getMarket(7L);

		assertThat(market.inventory()).isEmpty();
		assertThat(market.shopItems()).extracting(item -> item.catalogId()).contains("sword", "armor");
		assertThat(market.gold()).isEqualTo(500L);
		assertThat(market.objectStats()).isEmpty();
	}

	@Test
	void sumsStatsFromEquippedItemsForCharacterBonuses() {
		when(inventoryRepository.sumEquippedStatsByCharacterId(70L))
				.thenReturn(List.of(new Object[] { "strength", 8L }, new Object[] { "speed", 2L }));

		var market = marketService.getMarket(7L);

		assertThat(market.objectStats()).containsEntry("strength", 8L).containsEntry("speed", 2L);
	}

	@Test
	void buyingItemChargesServerCatalogPriceAndCreatesUnequippedInventoryItem() {
		var market = marketService.buy(7L, "sword");

		assertThat(market.gold()).isEqualTo(240L);
		assertThat(character.getGold()).isEqualTo(240L);
		verify(characterStatsRepository).save(character);
		verify(inventoryRepository).save(org.mockito.ArgumentMatchers.argThat(item ->
				item.getCharacterId().equals(70L)
						&& item.getCatalogId().equals("sword")
						&& item.getLevel() == 3
						&& item.getStats().equals(Map.of("strength", 8, "speed", 2))
						&& !item.getEquipped()));
	}

	@Test
	void buyingItemRejectsInsufficientGoldWithoutChangingBalance() {
		character.setGold(259L);

		assertThatThrownBy(() -> marketService.buy(7L, "sword"))
				.isInstanceOf(InvalidRequestException.class)
				.hasMessageContaining("Oro insuficiente.");

		assertThat(character.getGold()).isEqualTo(259L);
		verify(inventoryRepository, never()).save(any());
	}

	@Test
	void buyingItemRejectsAnItemAboveTheCharacterLevelWithoutChangingBalance() {
		assertThatThrownBy(() -> marketService.buy(7L, "bow"))
				.isInstanceOf(InvalidRequestException.class)
				.hasMessageContaining("Nivel insuficiente");

		assertThat(character.getGold()).isEqualTo(500L);
		verify(inventoryRepository, never()).save(any());
	}

	@Test
	void buyingItemAllowsAnItemAtExactlyTheCharacterLevel() {
		character.setGold(260L);

		var market = marketService.buy(7L, "sword");

		assertThat(market.gold()).isZero();
	}

	@Test
	void upgradingItemPersistsIncreasedJsonStats() {
		PlayerInventoryItem item = inventoryItem(102L, "sword", "Arma", false);
		item.setStats(Map.of("strength", 8, "speed", 2));
		when(inventoryRepository.findByIdAndCharacterId(102L, 70L)).thenReturn(Optional.of(item));

		marketService.upgrade(7L, 102L);

		assertThat(item.getLevel()).isEqualTo(4);
		assertThat(item.getStats()).containsEntry("strength", 9).containsEntry("speed", 3);
		assertThat(character.getGold()).isEqualTo(335L);
		verify(inventoryRepository).save(item);
	}

	@Test
	void sellingItemReturnsSeventyPercentOfThePriceAndRemovesIt() {
		PlayerInventoryItem item = inventoryItem(103L, "armor", "Armadura", false);
		when(inventoryRepository.findByIdAndCharacterId(103L, 70L)).thenReturn(Optional.of(item));
		character.setGold(50L);

		var market = marketService.sell(7L, 103L);

		assertThat(market.gold()).isEqualTo(288L);
		verify(inventoryRepository).delete(item);
	}

	@Test
	void sellingAnEquippedItemIsRejectedWithoutChangingAnything() {
		PlayerInventoryItem item = inventoryItem(103L, "armor", "Armadura", true);
		when(inventoryRepository.findByIdAndCharacterId(103L, 70L)).thenReturn(Optional.of(item));
		character.setGold(50L);

		assertThatThrownBy(() -> marketService.sell(7L, 103L))
				.isInstanceOf(InvalidRequestException.class)
				.hasMessageContaining("Desequipa el objeto antes de venderlo.");

		assertThat(character.getGold()).isEqualTo(50L);
		assertThat(item.getEquipped()).isTrue();
		verify(inventoryRepository, never()).delete(any());
	}

	@Test
	void equippingItemUnequipsOtherItemInTheSameSlot() {
		PlayerInventoryItem previouslyEquipped = inventoryItem(100L, "bow", "Arma", true);
		PlayerInventoryItem item = inventoryItem(101L, "sword", "Arma", false);
		when(inventoryRepository.findByIdAndCharacterId(101L, 70L)).thenReturn(Optional.of(item));
		when(inventoryRepository.findByCharacterIdOrderById(70L))
				.thenReturn(List.of(previouslyEquipped, item));

		marketService.setEquipped(7L, 101L, true);

		assertThat(item.getEquipped()).isTrue();
		verify(inventoryRepository).unequipOtherItemsInSlot(70L, "Arma", 101L);
		verify(inventoryRepository).save(item);
	}

	@Test
	void cannotEquipAnItemBelongingToAnotherCharacter() {
		when(inventoryRepository.findByIdAndCharacterId(101L, 70L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> marketService.setEquipped(7L, 101L, true))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Objeto no encontrado.");
	}

	private PlayerInventoryItem inventoryItem(Long id, String catalogId, String slot, boolean equipped) {
		PlayerInventoryItem item = new PlayerInventoryItem();
		ReflectionTestUtils.setField(item, "id", id);
		item.setCharacterId(70L);
		item.setCatalogId(catalogId);
		item.setSlot(slot);
		item.setLevel(3);
		item.setQuantity(1);
		item.setEquipped(equipped);
		item.setStats(Map.of("strength", 1));
		return item;
	}

	private static ToolIndexItem catalogItem(
			String catalogId, String name, String category, int price, int level, Map<String, Integer> stats) {
		ToolIndexItem item = new ToolIndexItem();
		ReflectionTestUtils.setField(item, "catalogId", catalogId);
		ReflectionTestUtils.setField(item, "name", name);
		ReflectionTestUtils.setField(item, "icon", "⚔️");
		ReflectionTestUtils.setField(item, "category", category);
		ReflectionTestUtils.setField(item, "rarity", "Rare");
		ReflectionTestUtils.setField(item, "baseLevel", level);
		ReflectionTestUtils.setField(item, "maxLevel", 100);
		ReflectionTestUtils.setField(item, "purchasePrice", price);
		ReflectionTestUtils.setField(item, "baseUpgradePrice", 55);
		ReflectionTestUtils.setField(item, "upgradeStatBuffs", 1);
		ReflectionTestUtils.setField(item, "baseStatsBuff", stats);
		return item;
	}
}
