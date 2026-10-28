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
@Table(name = "challenges")
public class Challenge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private String name;

	@Column(name = "chall_reward_id")
	private Long rewardId;

	@Column(name = "group_id")
	private Long groupId;
	private String description;

	@Column(name = "dificulty", nullable = false)
	@ColumnTransformer(write = "?::public.difficulty_tier")
	private String difficulty;

	@Column(nullable = false)
	private boolean completed;

	@Column(name = "task_id")
	private Long taskId;

	@Column(name = "user_id")
	private Long userId;
	private Short rating;

	@Column(name = "visibility")
	@ColumnTransformer(write = "?::public.visibility_type")
	private String visibility;

	private String requirement;

	public Long getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getRewardId() {
		return rewardId;
	}

	public void setRewardId(Long rewardId) {
		this.rewardId = rewardId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Short getRating() {
		return rating;
	}

	public void setRating(Short rating) {
		this.rating = rating;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getRequirement() {
		return requirement;
	}

	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}
}
