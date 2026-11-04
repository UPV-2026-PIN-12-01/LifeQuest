package com.lifequest.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.RewardSource;

public interface RewardRepository extends JpaRepository<Reward, Long> {

	@Query("select coalesce(sum(r.xpReward), 0) from Reward r "
			+ "where r.userId = :userId and r.source = :source "
			+ "and r.claimedAt >= :from and r.claimedAt < :toExclusive")
	long sumClaimedXpBySource(
			@Param("userId") Long userId,
			@Param("source") RewardSource source,
			@Param("from") Instant from,
			@Param("toExclusive") Instant toExclusive);

	@Query("select count(r) from Reward r "
			+ "where r.userId = :userId and r.claimedAt >= :from and r.claimedAt < :toExclusive")
	long countClaimed(Long userId, Instant from, Instant toExclusive);
}
