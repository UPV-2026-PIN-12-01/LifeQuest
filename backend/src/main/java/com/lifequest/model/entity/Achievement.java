package com.lifequest.model.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "achievements")
@DynamicInsert
public class Achievement {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "acquired_at", nullable = false)
	private Instant acquiredAt;
	@Column(name = "user_id", nullable = false)
	private Long userId;
	@Column(name = "index_id", nullable = false)
	private Long indexId;
	@Column(name = "reward_id")
	private Long rewardId;

	public Long getId() { return id; }
	public Instant getAcquiredAt() { return acquiredAt; }
	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public Long getIndexId() { return indexId; }
	public void setIndexId(Long indexId) { this.indexId = indexId; }
	public Long getRewardId() { return rewardId; }
	public void setRewardId(Long rewardId) { this.rewardId = rewardId; }
}
