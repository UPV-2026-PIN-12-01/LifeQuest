package com.lifequest.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifequest.model.entity.Goal;

public interface GoalRepository extends JpaRepository<Goal, Long> {

	List<Goal> findByUserIdAndCompletedOrderByCreatedAtDesc(Long userId, boolean completed);

	long countByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
			Long userId, Instant from, Instant to);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select goal from Goal goal where goal.id = :goalId and goal.userId = :userId")
	Optional<Goal> findForUpdateByIdAndUserId(@Param("goalId") Long goalId, @Param("userId") Long userId);
}
