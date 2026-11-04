package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.Goal;

public interface GoalRepository extends JpaRepository<Goal, Long> {

	List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<Goal> findByIdAndUserId(Long id, Long userId);
}
