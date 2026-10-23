package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lifequest.model.entity.CharacterStats;
import com.lifequest.repository.CharacterStatsRepository;

class CharacterStatsServiceTest {

	@Test
	void addXpLevelsUpAndKeepsTotalValidForTheDbCheck() {
		CharacterStatsRepository repository = mock(CharacterStatsRepository.class);
		CharacterStats stats = new CharacterStats();
		stats.setLevel(1);
		stats.setXpCurrent(10L);
		stats.setXpTotal(10L);
		when(repository.findByUserId(7L)).thenReturn(Optional.of(stats));
		when(repository.save(any(CharacterStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Level 1 needs 13 and level 2 needs 15: 10 + 30 = 40 -> level 3 with 12.
		new CharacterStatsService(repository).addXp(7L, 30);

		assertThat(stats.getLevel()).isEqualTo(3);
		assertThat(stats.getXpCurrent()).isEqualTo(12L);
		assertThat(stats.getXpTotal()).isEqualTo(12L + 9 + 30);
	}
}
