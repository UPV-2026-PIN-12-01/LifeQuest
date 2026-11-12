package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifequest.model.entity.PlayerInventoryItem;

public interface PlayerInventoryRepository extends JpaRepository<PlayerInventoryItem, Long> {

	List<PlayerInventoryItem> findByCharacterIdOrderById(Long characterId);

	Optional<PlayerInventoryItem> findByIdAndCharacterId(Long id, Long characterId);

	Optional<PlayerInventoryItem> findFirstByCharacterIdAndCatalogIdOrderById(
			Long characterId, String catalogId);

	@Modifying
	@Query("""
			update PlayerInventoryItem item
			set item.equipped = false
			where item.characterId = :characterId
				and item.slot = :slot
				and item.id <> :excludedItemId
				and item.equipped = true
			""")
	int unequipOtherItemsInSlot(
			@Param("characterId") Long characterId,
			@Param("slot") String slot,
			@Param("excludedItemId") Long excludedItemId);

	@Query(value = """
			SELECT stat.key, SUM((stat.value #>> '{}')::bigint)
			FROM public.player_inventory inventory
			CROSS JOIN LATERAL jsonb_each(inventory.stats) AS stat(key, value)
			WHERE inventory.character_id = :characterId
				AND inventory.equipped
			GROUP BY stat.key
			""", nativeQuery = true)
	List<Object[]> sumEquippedStatsByCharacterId(@Param("characterId") Long characterId);
}
