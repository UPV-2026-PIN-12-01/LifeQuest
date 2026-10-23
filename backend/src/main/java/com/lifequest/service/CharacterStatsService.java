package com.lifequest.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.lifequest.exception.DuplicateUserException;
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


	// Levels up while the XP covers the next level. xpTotal is derived so it always passes
	// stats_xp_rules_check: at level 1 it equals xpCurrent, above it is xpCurrent + L^2 + 10L.
	@Transactional
	public CharacterStats addXp(Long userId, long xp) {
		CharacterStats stats = getByUserId(userId);
		int level = stats.getLevel() == null ? 1 : stats.getLevel();
		long current = (stats.getXpCurrent() == null ? 0 : stats.getXpCurrent()) + xp;
		while (current >= xpForNextLevel(level)) {
			current -= xpForNextLevel(level);
			level++;
		}
		stats.setLevel(level);
		stats.setXpCurrent(current);
		stats.setXpTotal(level == 1 ? current : current + (long) level * level + 10L * level);
		return characterStatsRepository.save(stats);
	}

	@Transactional(readOnly = true)
	public boolean existsByPlayerName(String playerName) {
		return characterStatsRepository.existsByPlayerName(playerName);
	}

	// Own name is free. Another character's name is 409.
	@Transactional(readOnly = true)
	public void ensurePlayerNameAvailable(Long userId, String playerName) {
		CharacterStats stats = getByUserId(userId);
		if (playerName.equals(stats.getPlayerName())) {
			return;
		}
		if (characterStatsRepository.existsByPlayerNameAndUserIdNot(playerName, userId)) {
			throw new DuplicateUserException("Player name already exists");
		}
	}

	// Flush the character row first so users.playerName can follow it.
	@Transactional
	public void renameForUser(Long userId, String playerName) {
		CharacterStats stats = characterStatsRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Character not found"));
		if (playerName.equals(stats.getPlayerName())) {
			return;
		}
		if (characterStatsRepository.existsByPlayerNameAndUserIdNot(playerName, userId)) {
			throw new DuplicateUserException("Player name already exists");
		}
		stats.setPlayerName(playerName);
		try {
			characterStatsRepository.saveAndFlush(stats);
		} catch (DataIntegrityViolationException ex) {
			throw new DuplicateUserException("Player name already exists");
		}
	}

	@Transactional
	public CharacterStats createForUser(Long userId, String playerName, PlayerClass playerClass) {
		CharacterStats characterStats = new CharacterStats();
		characterStats.setUserId(userId);
		characterStats.setPlayerName(playerName);
		characterStats.setPlayerClass(playerClass);
		return characterStatsRepository.save(characterStats);
	}

	public CharacterStats getByUserId(Long userId) {
		return characterStatsRepository.findByUserId(userId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Character stats not found for user ID: " + userId));
	}
}
