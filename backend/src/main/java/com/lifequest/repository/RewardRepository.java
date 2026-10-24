package com.lifequest.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lifequest.model.entity.Reward;

public interface RewardRepository extends JpaRepository<Reward, Long> {

	@Query("select coalesce(sum(r.xpReward), 0) from Reward r "
			+ "where r.userId = :userId and r.claimedAt >= :from and r.claimedAt < :toExclusive")
	long sumClaimedXp(Long userId, Instant from, Instant toExclusive);
}
