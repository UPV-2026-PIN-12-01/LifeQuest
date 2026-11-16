package com.lifequest.model.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@DynamicInsert
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private String username;

	@Column(name = "profile_url")
	private String profileUrl;

	@Column(name = "playerName")
	private String playerName;

	@Column(nullable = false)
	private String email;

	@Column(name = "inner_id", nullable = false, unique = true)
	private UUID innerId;

	@Column(name = "user_icon")
	private String userIcon;

	@Column(name = "player_id")
	private Long playerId;

	@Column(name = "task_streak", nullable = false)
	private Integer taskStreak = 0;

	@Column(name = "day_streak", nullable = false)
	private Integer dayStreak = 0;

	@Column(name = "last_activity_date")
	private LocalDate lastActivityDate;

	@Column(name = "xp_acc_boost", nullable = false)
	private Float xpAccBoost = 1.0f;

	@Column(name= "max_day_streak", nullable = false)
	private Integer maxDayStreak = 0;

	public Long getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UUID getInnerId() {
		return innerId;
	}

	public void setInnerId(UUID innerId) {
		this.innerId = innerId;
	}

	public String getUserIcon() {
		return userIcon;
	}

	public void setUserIcon(String userIcon) {
		this.userIcon = userIcon;
	}

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public Integer getTaskStreak() {
		return taskStreak;
	}

	public void setTaskStreak(Integer taskStreak) {
		this.taskStreak = taskStreak;
	}

	public Integer getDayStreak() {
		return dayStreak;
	}

	public void setDayStreak(Integer dayStreak) {
		this.dayStreak = dayStreak;
	}

	public LocalDate getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(LocalDate lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	public Float getXpAccBoost() {
		return xpAccBoost;
	}

	public void setXpAccBoost(Float xpAccBoost) {
		this.xpAccBoost = xpAccBoost;
	}

	public int getStreakDays() {
		return dayStreak == null ? 0 : dayStreak;
	}

	public Integer getMaxDayStreak() {
		return maxDayStreak;
	}

	public void setMaxDayStreak(Integer maxDayStreak) {
		this.maxDayStreak = maxDayStreak;
	}
}
