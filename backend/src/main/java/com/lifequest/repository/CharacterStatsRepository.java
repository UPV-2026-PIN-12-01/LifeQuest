package com.lifequest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.CharacterStats;

public interface CharacterStatsRepository extends JpaRepository<CharacterStats, Long> {

	Optional<CharacterStats> findByUserId(Long userId);

	Optional<CharacterStats> findByPlayerNameIgnoreCase(String playerName);

	List<CharacterStats> findByUserIdIn(Collection<Long> userIds);

	boolean existsByPlayerName(String playerName);

	boolean existsByPlayerNameAndUserIdNot(String playerName, Long userId);
}
