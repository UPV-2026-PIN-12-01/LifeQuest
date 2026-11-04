package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.lifequest.model.dto.GoalDto;
import com.lifequest.model.entity.Goal;
import com.lifequest.repository.GoalRepository;

class GoalServiceTest {

	@Test
	void listsOnlyVisibleGoalsReturnedForTheAuthenticatedUser() {
		GoalRepository repository = mock(GoalRepository.class);
		UserService userService = mock(UserService.class);
		RewardService rewardService = mock(RewardService.class);
		CharacterStatsService characterStatsService = mock(CharacterStatsService.class);
		Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);

		Goal visibleGoal = new Goal();
		visibleGoal.setName("Terminar el curso");
		visibleGoal.setDescription("Completar las lecciones");
		visibleGoal.setStartDate(Instant.parse("2026-11-01T00:00:00Z"));
		visibleGoal.setEndDate(Instant.parse("2026-11-30T00:00:00Z"));
		visibleGoal.setVisibility("SHOWN");

		Goal deletedGoal = new Goal();
		deletedGoal.setName("Objetivo eliminado");
		deletedGoal.setVisibility("DELETED");
		when(repository.findByUserIdOrderByCreatedAtDesc(7L))
				.thenReturn(List.of(visibleGoal, deletedGoal));

		GoalService service = new GoalService(
				repository, userService, rewardService, characterStatsService, clock);
		List<GoalDto> goals = service.findForUser(7L);

		assertThat(goals).hasSize(1);
		assertThat(goals.get(0).title()).isEqualTo("Terminar el curso");
		assertThat(goals.get(0).startDate().toString()).isEqualTo("2026-11-01");
		assertThat(goals.get(0).status()).isEqualTo("pending");
		verify(repository).findByUserIdOrderByCreatedAtDesc(7L);
	}
}
