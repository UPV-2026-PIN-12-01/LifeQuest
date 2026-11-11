package com.lifequest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.CharacterStats;

public interface CharacterStatsRepository extends JpaRepository<CharacterStats, Long> {

	Optional<CharacterStats> findByUserId(Long userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select stats from CharacterStats stats where stats.userId = :userId")
	Optional<CharacterStats> findByUserIdForUpdate(@Param("userId") Long userId);

	Optional<CharacterStats> findByPlayerNameIgnoreCase(String playerName);

	List<CharacterStats> findByUserIdIn(Collection<Long> userIds);

	boolean existsByPlayerName(String playerName);

	boolean existsByPlayerNameAndUserIdNot(String playerName, Long userId);
}
