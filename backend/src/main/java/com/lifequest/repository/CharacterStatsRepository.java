package com.lifequest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.CharacterStats;

public interface CharacterStatsRepository extends JpaRepository<CharacterStats, Long> {

	Optional<CharacterStats> findByUserId(Long userId);
}
