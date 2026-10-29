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
	private Long hp = 20L;

	@Column(name = "strength", nullable = false)
	private Integer strength = 5;

	@Column(name = "speed", nullable = false)
	private Integer speed = 5;

	@Column(name = "stamina", nullable = false)
	private Integer stamina = 5;

	@Column(name = "wisdom", nullable = false)
	private Integer wisdom = 5;

	@Column(name = "level", nullable = false)
	private Integer level = 1;

	@Column(name = "xpTotal", nullable = false)
	private Long xpTotal = 0L;

	@Column(name = "xpCurrent", nullable = false)
	private Long xpCurrent = 0L;

	@Column(name = "gold", nullable = false)
	private Long gold = 0L;

	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "class", nullable = false)
	private PlayerClass playerClass = PlayerClass.knight;

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

	public void setHp(Long hp) {
		this.hp = hp;
	}

	public Integer getStrength() {
		return strength;
	}

	public void setStrength(Integer strength) {
		this.strength = strength;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	public Integer getStamina() {
		return stamina;
	}

	public void setStamina(Integer stamina) {
		this.stamina = stamina;
	}

	public Integer getWisdom() {
		return wisdom;
	}

	public void setWisdom(Integer wisdom) {
		this.wisdom = wisdom;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Long getXpTotal() {
		return xpTotal;
	}

	public void setXpTotal(Long xpTotal) {
		this.xpTotal = xpTotal;
	}

	public Long getXpCurrent() {
		return xpCurrent;
	}

	public void setXpCurrent(Long xpCurrent) {
		this.xpCurrent = xpCurrent;
	}

	public Long getGold() {
		return gold;
	}

	public void setGold(Long gold) {
		this.gold = gold;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public void setPlayerClass(PlayerClass playerClass) {
		this.playerClass = playerClass;
	}
}
