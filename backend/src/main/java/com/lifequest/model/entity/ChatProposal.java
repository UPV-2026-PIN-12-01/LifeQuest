package com.lifequest.model.entity;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_proposals")
public class ChatProposal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "thread_id", nullable = false)
	private Long threadId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "message_id", nullable = false)
	private Long messageId;

	@Column(nullable = false)
	private String kind;

	@Column(nullable = false)
	private String title;

	@Column(name = "when_label", nullable = false)
	private String whenLabel;

	@Column(nullable = false)
	private String why;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private String payload;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public Long getId() {
		return id;
	}

	public Long getThreadId() {
		return threadId;
	}

	public void setThreadId(Long threadId) {
		this.threadId = threadId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWhenLabel() {
		return whenLabel;
	}

	public void setWhenLabel(String whenLabel) {
		this.whenLabel = whenLabel;
	}

	public String getWhy() {
		return why;
	}

	public void setWhy(String why) {
		this.why = why;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
