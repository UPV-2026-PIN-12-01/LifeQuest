package com.lifequest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

	List<Challenge> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<Challenge> findByIdAndUserId(Long id, Long userId);
}
