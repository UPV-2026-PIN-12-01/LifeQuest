package com.lifequest.model.entity;

import java.time.Instant;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// The check-in of a task: started when the user taps Empezar, ended on complete or skip.
@Entity
@Table(name = "task_sessions")
@DynamicInsert
public class TaskSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "task_id", nullable = false)
	private Long taskId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "last_heartbeat_at", nullable = false)
	private Instant lastHeartbeatAt;

	@Column(name = "focused_seconds", nullable = false)
	private long focusedSeconds;

	@Column(name = "ended_at")
	private Instant endedAt;

	public Long getId() {
		return id;
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

	public Instant getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {
		this.startedAt = startedAt;
	}

	public Instant getLastHeartbeatAt() {
		return lastHeartbeatAt;
	}

	public void setLastHeartbeatAt(Instant lastHeartbeatAt) {
		this.lastHeartbeatAt = lastHeartbeatAt;
	}

	public long getFocusedSeconds() {
		return focusedSeconds;
	}

	public void setFocusedSeconds(long focusedSeconds) {
		this.focusedSeconds = focusedSeconds;
	}

	public Instant getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(Instant endedAt) {
		this.endedAt = endedAt;
	}

	public boolean isOpen() {
		return endedAt == null;
	}
}
