package com.lifequest.model.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "friendships")
public class Friendship {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "requester_id", nullable = false)
	private Long requesterId;

	@Column(name = "addressee_id", nullable = false)
	private Long addresseeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FriendshipStatus status = FriendshipStatus.PENDING;

	@Column(name = "accepted_at")
	private Instant acceptedAt;

	public Long getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Long getRequesterId() {
		return requesterId;
	}

	public void setRequesterId(Long requesterId) {
		this.requesterId = requesterId;
	}

	public Long getAddresseeId() {
		return addresseeId;
	}

	public void setAddresseeId(Long addresseeId) {
		this.addresseeId = addresseeId;
	}

	public FriendshipStatus getStatus() {
		return status;
	}

	public void setStatus(FriendshipStatus status) {
		this.status = status;
	}

	public Instant getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(Instant acceptedAt) {
		this.acceptedAt = acceptedAt;
	}

	public boolean involves(Long userId) {
		return userId.equals(requesterId) || userId.equals(addresseeId);
	}

	public Long otherUserId(Long userId) {
		return userId.equals(requesterId) ? addresseeId : requesterId;
	}
}
