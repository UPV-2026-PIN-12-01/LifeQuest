package com.lifequest.model.entity;

import java.time.Instant;

import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "goals")
public class Goal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "goal_reward_id")
	private Long rewardId;

	@Column(name = "group_id")
	private Long groupId;

	@Column(name = "description")
	private String description;

	@Column(name = "start_date")
	private Instant startDate;

	@Column(name = "end_date")
	private Instant endDate;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "rating")
	private Short rating;

	@Column(name = "completed")
	private boolean completed;

	@Column(name = "visibility")
	@ColumnTransformer(write = "?::public.visibility_type")
	private String visibility;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Long getRewardId() {
		return rewardId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public String getDescription() {
		return description;
	}

	public Instant getStartDate() {
		return startDate;
	}

	public Instant getEndDate() {
		return endDate;
	}

	public Long getUserId() {
		return userId;
	}

	public Short getRating() {
		return rating;
	}

	public boolean isCompleted() {
		return completed;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRewardId(Long rewardId) {
		this.rewardId = rewardId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStartDate(Instant startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Instant endDate) {
		this.endDate = endDate;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setRating(Short rating) {
		this.rating = rating;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
}
