package com.lifequest.model.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "character_stats")
@DynamicInsert
public class CharacterStats {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "playerName", nullable = false)
	private String playerName;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "hp", nullable = false)
	private Long hp;

	@Column(name = "strength", nullable = false)
	private Integer strength;

	@Column(name = "speed", nullable = false)
	private Integer speed;

	@Column(name = "stamina", nullable = false)
	private Integer stamina;

	@Column(name = "wisdom", nullable = false)
	private Integer wisdom;

	@Column(name = "level", nullable = false)
	private Integer level;

	@Column(name = "xpTotal", nullable = false)
	private Long xpTotal;

	@Column(name = "xpCurrent", nullable = false)
	private Long xpCurrent;

	@Column(name = "gold", nullable = false)
	private Long gold;

	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "class", nullable = false)
	private PlayerClass playerClass = PlayerClass.knight;

	// Read-only here: the DB fills the defaults and its CHECK guards the XP rules.
	@Column(insertable = false, updatable = false)
	private Integer level;

	@Column(name = "xpCurrent", insertable = false, updatable = false)
	private Long xpCurrent;

	public Long getId() {
		return id;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getHp() {
		return hp;
	}

	public Integer getStrength() {
		return strength;
	}

	public Integer getSpeed() {
		return speed;
	}

	public Integer getStamina() {
		return stamina;
	}

	public Integer getWisdom() {
		return wisdom;
	}

	public Integer getLevel() {
		return level;
	}

	public Long getXpTotal() {
		return xpTotal;
	}

	public Long getXpCurrent() {
		return xpCurrent;
	}

	public Long getGold() {
		return gold;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public void setPlayerClass(PlayerClass playerClass) {
		this.playerClass = playerClass;
	}

	public Integer getLevel() {
		return level;
	}

	public Long getXpCurrent() {
		return xpCurrent;
	}
}
