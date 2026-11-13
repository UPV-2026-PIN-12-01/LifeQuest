package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.ChatThread;

public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {

	Optional<ChatThread> findByIdAndUserId(Long id, Long userId);

	List<ChatThread> findByUserIdOrderByUpdatedAtDescIdDesc(Long userId);
}
