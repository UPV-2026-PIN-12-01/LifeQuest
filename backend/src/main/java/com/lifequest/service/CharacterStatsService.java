package com.lifequest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.PlayerClass;
import com.lifequest.repository.CharacterStatsRepository;

@Service
public class CharacterStatsService {

	private final CharacterStatsRepository characterStatsRepository;

	public CharacterStatsService(CharacterStatsRepository characterStatsRepository) {
		this.characterStatsRepository = characterStatsRepository;
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
