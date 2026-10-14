package com.lifequest.model.entity;

import java.time.Instant;
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
}
