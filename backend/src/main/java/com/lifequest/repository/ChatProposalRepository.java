package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.lifequest.model.entity.ChatProposal;

public interface ChatProposalRepository extends JpaRepository<ChatProposal, Long> {

	List<ChatProposal> findByThreadIdAndUserIdOrderByIdAsc(Long threadId, Long userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<ChatProposal> findByIdAndUserId(Long id, Long userId);
}
