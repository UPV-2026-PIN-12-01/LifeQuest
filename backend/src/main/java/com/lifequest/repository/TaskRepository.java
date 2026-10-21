package com.lifequest.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
			Long userId, Instant from, Instant toExclusive);

	Optional<Task> findByIdAndUserId(Long id, Long userId);

	boolean existsByRewardId(Long rewardId);
}
