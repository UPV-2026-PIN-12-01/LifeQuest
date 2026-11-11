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
@Table(name = "tool_index")
public class ToolIndexItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "catalog_id", unique = true)
	private String catalogId;

	private String name;

	private String icon;

	@Column(name = "tool_type")
	private String category;

	private String rarity;

	@Column(name = "base_level")
	private Integer baseLevel;

	@Column(name = "max_level")
	private Integer maxLevel;

	@Column(name = "purchase_price")
	private Integer purchasePrice;

	@Column(name = "base_upgrade_price")
	private Integer baseUpgradePrice;

	@Column(name = "upgrade_stat_buffs")
	private Integer upgradeStatBuffs;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "base_stats_buff", columnDefinition = "jsonb")
	private Map<String, Integer> baseStatsBuff;

	private Integer heal;

	@Column(name = "stamina_restore")
	private Integer staminaRestore;

	public Long getId() {
		return id;
	}

	public String getCatalogId() {
		return catalogId;
	}

	public String getName() {
		return name;
	}

	public String getIcon() {
		return icon;
	}

	public String getCategory() {
		return category;
	}

	public String getRarity() {
		return rarity;
	}

	public Integer getBaseLevel() {
		return baseLevel;
	}

	public Integer getMaxLevel() {
		return maxLevel;
	}

	public Integer getPurchasePrice() {
		return purchasePrice;
	}

	public Integer getBaseUpgradePrice() {
		return baseUpgradePrice;
	}

	public Integer getUpgradeStatBuffs() {
		return upgradeStatBuffs;
	}

	public Map<String, Integer> getBaseStatsBuff() {
		return baseStatsBuff;
	}

	public Integer getHeal() {
		return heal;
	}

	public Integer getStaminaRestore() {
		return staminaRestore;
	}
}
