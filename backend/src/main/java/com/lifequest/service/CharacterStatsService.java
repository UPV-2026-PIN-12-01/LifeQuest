package com.lifequest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.PlayerClass;
import com.lifequest.repository.CharacterStatsRepository;

@Service
public class CharacterStatsService {

	private final CharacterStatsRepository characterStatsRepository;

	public CharacterStatsService(CharacterStatsRepository characterStatsRepository) {
		this.characterStatsRepository = characterStatsRepository;
	}

	// XP needed to go from level to level + 1. Must match the stats_xp_rules_check CHECK in the DB,
	// where reaching level L takes L^2 + 10L total XP.
	public static long xpForNextLevel(int level) {
		return 2L * level + 11;
	}

	@Transactional(readOnly = true)
	public CharacterStats getByUserId(Long userId) {
		return characterStatsRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Character not found"));
	}

	@Transactional(readOnly = true)
	public boolean existsByPlayerName(String playerName) {
		return characterStatsRepository.existsByPlayerName(playerName);
	}

	@Transactional
	public CharacterStats createForUser(Long userId, String playerName, PlayerClass playerClass) {
		CharacterStats characterStats = new CharacterStats();
		characterStats.setUserId(userId);
		characterStats.setPlayerName(playerName);
		characterStats.setPlayerClass(playerClass);
		return characterStatsRepository.save(characterStats);
	}
}
