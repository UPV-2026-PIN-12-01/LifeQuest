package com.lifequest.model.entity;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_inventory")
public class PlayerInventoryItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "character_id", nullable = false)
	private Long characterId;

	@Column(name = "catalog_id", nullable = false)
	private String catalogId;

	@Column(name = "slot", nullable = false)
	private String slot;

	@Column(nullable = false)
	private Integer level;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Boolean equipped;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private Map<String, Integer> stats;

	public Long getId() {
		return id;
	}

	public Long getCharacterId() {
		return characterId;
	}

	public void setCharacterId(Long characterId) {
		this.characterId = characterId;
	}

	public String getCatalogId() {
		return catalogId;
	}

	public void setCatalogId(String catalogId) {
		this.catalogId = catalogId;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Boolean getEquipped() {
		return equipped;
	}

	public void setEquipped(Boolean equipped) {
		this.equipped = equipped;
	}

	public Map<String, Integer> getStats() {
		return stats;
	}

	public void setStats(Map<String, Integer> stats) {
		this.stats = stats;
	}
}
