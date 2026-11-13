package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByThreadIdOrderByIdAsc(Long threadId);

	List<ChatMessage> findByThreadIdOrderByIdDesc(Long threadId, Pageable pageable);

	Optional<ChatMessage> findFirstByThreadIdOrderByIdDesc(Long threadId);
}
