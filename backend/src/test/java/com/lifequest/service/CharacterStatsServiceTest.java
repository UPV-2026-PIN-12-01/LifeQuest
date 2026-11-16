package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lifequest.model.entity.CharacterStats;
import com.lifequest.repository.CharacterStatsRepository;
import com.lifequest.repository.TaskRepository;

class CharacterStatsServiceTest {

	@Test
	void addXpLevelsUpAndKeepsTotalValidForTheDbCheck() {
		CharacterStatsRepository repository = mock(CharacterStatsRepository.class);
		UserStreakService streakService = mock(UserStreakService.class);
		CharacterStats stats = new CharacterStats();
		stats.setLevel(1);
		stats.setXpCurrent(10L);
		stats.setXpTotal(10L);
		when(repository.findByUserId(7L)).thenReturn(Optional.of(stats));
		when(repository.save(any(CharacterStats.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(streakService.recordXpActivity(7L, false)).thenReturn(1.0);

		// Level 1 needs 13 and level 2 needs 15: 10 + 30 = 40 -> level 3 with 12.
		service(repository, streakService).addXp(7L, 30);

		assertThat(stats.getLevel()).isEqualTo(3);
		assertThat(stats.getXpCurrent()).isEqualTo(12L);
		assertThat(stats.getXpTotal()).isEqualTo(12L + 9 + 30);
		verify(streakService).recordXpActivity(7L, false);
	}

	@Test
	void appliesAndReturnsTheDayStreakXpBoost() {
		CharacterStatsRepository repository = mock(CharacterStatsRepository.class);
		UserStreakService streakService = mock(UserStreakService.class);
		CharacterStats stats = new CharacterStats();
		stats.setLevel(1);
		stats.setXpCurrent(0L);
		stats.setXpTotal(0L);
		when(repository.findByUserId(7L)).thenReturn(Optional.of(stats));
		when(repository.save(any(CharacterStats.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(streakService.recordXpActivity(7L, false)).thenReturn(1.15);

		long awardedXp = service(repository, streakService).addXp(7L, 20);

		assertThat(awardedXp).isEqualTo(23);
		assertThat(stats.getXpCurrent()).isEqualTo(10L);
	}

	@Test
	void taskXpActivityHandlesUsersWithoutTasks() {
		CharacterStatsRepository repository = mock(CharacterStatsRepository.class);
		TaskRepository taskRepository = mock(TaskRepository.class);
		UserStreakService streakService = mock(UserStreakService.class);
		when(taskRepository.findFirstByUserIdOrderByStartTimeDesc(7L)).thenReturn(Optional.empty());
		when(streakService.recordXpActivity(7L, false)).thenReturn(1.0);

		double multiplier = new CharacterStatsService(repository, streakService, taskRepository,
				Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneId.of("Europe/Madrid")))
				.recordXpActivityForTask(7L);

		assertThat(multiplier).isEqualTo(1.0);
		verify(streakService).recordXpActivity(7L, false);
	}

	private static CharacterStatsService service(
			CharacterStatsRepository repository, UserStreakService streakService) {
		return new CharacterStatsService(repository, streakService, mock(TaskRepository.class),
				Clock.systemUTC());
	}
}
