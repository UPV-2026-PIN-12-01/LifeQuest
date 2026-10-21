package com.lifequest.model.entity;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rewards")
@DynamicInsert
public class Reward {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "xp_reward", nullable = false)
	private Long xpReward;

	@Column(name = "gold_reward", nullable = false)
	private Long goldReward;

	@Column(name = "user_id")
	private Long userId;

	public Long getId() {
		return id;
	}

	public Long getXpReward() {
		return xpReward;
	}

	public void setXpReward(Long xpReward) {
		this.xpReward = xpReward;
	}

	public Long getGoldReward() {
		return goldReward;
	}

	public void setGoldReward(Long goldReward) {
		this.goldReward = goldReward;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
