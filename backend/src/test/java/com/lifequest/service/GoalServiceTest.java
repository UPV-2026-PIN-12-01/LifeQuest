package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lifequest.exception.InvalidStateException;
import com.lifequest.model.dto.GoalDto;
import com.lifequest.model.dto.GoalRequestDto;
import com.lifequest.model.entity.Goal;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.RewardSource;
import com.lifequest.repository.GoalRepository;

class GoalServiceTest {

	@Test
	void listsOnlyVisibleGoalsForRequestedActiveHistoryTab() {
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
		visibleGoal.setRewardId(9L);
		visibleGoal.setVisibility("SHOWN");
		Reward reward = new Reward();
		reward.setXpReward(100L);
		reward.setGoldReward(50L);

		Goal deletedGoal = new Goal();
		deletedGoal.setName("Objetivo eliminado");
		deletedGoal.setVisibility("DELETED");
		when(repository.findByUserIdAndCompletedOrderByCreatedAtDesc(7L, false))
				.thenReturn(List.of(visibleGoal, deletedGoal));
		when(rewardService.getById(9L)).thenReturn(reward);

		GoalService service = new GoalService(
				repository, userService, rewardService, characterStatsService, clock);
		List<GoalDto> goals = service.findForUser(7L, false);

		assertThat(goals).hasSize(1);
		assertThat(goals.get(0).title()).isEqualTo("Terminar el curso");
		assertThat(goals.get(0).startDate().toString()).isEqualTo("2026-11-01T00:00");
		assertThat(goals.get(0).status()).isEqualTo("pending");
		assertThat(goals.get(0).rewardTier()).isEqualTo(1);
		assertThat(goals.get(0).xpReward()).isEqualTo(100);
		verify(repository).findByUserIdAndCompletedOrderByCreatedAtDesc(7L, false);
	}

	@Test
	void rejectsCreationWhenWeeklyLimitIsReached() {
		GoalRepository repository = mock(GoalRepository.class);
		UserService userService = mock(UserService.class);
		RewardService rewardService = mock(RewardService.class);
		CharacterStatsService characterStatsService = mock(CharacterStatsService.class);
		Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);
		when(repository.countByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
				eq(7L), any(Instant.class), any(Instant.class))).thenReturn(3L);

		GoalService service = new GoalService(
				repository, userService, rewardService, characterStatsService, clock);
		GoalRequestDto request = new GoalRequestDto(
				"Leer un libro",
				"Terminar el libro elegido",
				java.time.LocalDateTime.parse("2026-11-05T10:00"),
				java.time.LocalDateTime.parse("2026-11-05T22:00"),
				1);

		assertThatThrownBy(() -> service.create(7L, request))
				.isInstanceOf(InvalidStateException.class)
				.hasMessageContaining("three goals per week");
		verify(userService).lockForUpdate(7L);
	}

	@Test
	void createsGoalRewardWithObjectiveSource() {
		GoalRepository repository = mock(GoalRepository.class);
		UserService userService = mock(UserService.class);
		RewardService rewardService = mock(RewardService.class);
		CharacterStatsService characterStatsService = mock(CharacterStatsService.class);
		Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);
		when(rewardService.create(7L, 100L, 50L, RewardSource.OBJECTIVE)).thenReturn(9L);
		when(repository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
		Reward reward = new Reward();
		reward.setXpReward(100L);
		reward.setGoldReward(50L);
		when(rewardService.getById(9L)).thenReturn(reward);
		GoalService service = new GoalService(
				repository, userService, rewardService, characterStatsService, clock);
		GoalRequestDto request = new GoalRequestDto(
				"Leer un libro",
				"Terminar el libro elegido",
				java.time.LocalDateTime.parse("2026-11-05T10:00"),
				java.time.LocalDateTime.parse("2026-11-05T22:00"),
				1);

		service.create(7L, request);

		verify(rewardService).create(7L, 100L, 50L, RewardSource.OBJECTIVE);
	}

	@Test
	void preventsRewardClaimBeforeTierWaitHasElapsed() {
		GoalRepository repository = mock(GoalRepository.class);
		UserService userService = mock(UserService.class);
		RewardService rewardService = mock(RewardService.class);
		CharacterStatsService characterStatsService = mock(CharacterStatsService.class);
		Clock clock = Clock.fixed(Instant.parse("2026-11-05T11:59:59Z"), ZoneOffset.UTC);

		Goal goal = new Goal();
		goal.setUserId(7L);
		goal.setRewardId(9L);
		goal.setCompleted(true);
		goal.setRating((short) 8);
		goal.setEndDate(Instant.parse("2026-11-04T12:00:00Z"));
		goal.setVisibility("SHOWN");
		Reward reward = new Reward();
		reward.setXpReward(100L);
		reward.setGoldReward(50L);
		when(repository.findForUpdateByIdAndUserId(4L, 7L)).thenReturn(Optional.of(goal));
		when(rewardService.getById(9L)).thenReturn(reward);

		GoalService service = new GoalService(
				repository, userService, rewardService, characterStatsService, clock);

		assertThatThrownBy(() -> service.claim(7L, 4L))
				.isInstanceOf(InvalidStateException.class)
				.hasMessageContaining("not available");
	}

	@Test
	void listsLegacyRewardWithoutMislabelingItAsDiamondAndUsesMaximumWait() {
		GoalRepository repository = mock(GoalRepository.class);
		UserService userService = mock(UserService.class);
		RewardService rewardService = mock(RewardService.class);
		CharacterStatsService characterStatsService = mock(CharacterStatsService.class);
		Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);

		Goal goal = new Goal();
		goal.setName("Objetivo antiguo");
		goal.setRewardId(9L);
		goal.setEndDate(Instant.parse("2026-11-01T12:00:00Z"));
		goal.setVisibility("SHOWN");
		Reward reward = new Reward();
		reward.setXpReward(200L);
		reward.setGoldReward(75L);
		when(repository.findByUserIdAndCompletedOrderByCreatedAtDesc(7L, false))
				.thenReturn(List.of(goal));
		when(rewardService.getById(9L)).thenReturn(reward);

		GoalService service = new GoalService(
				repository, userService, rewardService, characterStatsService, clock);
		GoalDto dto = service.findForUser(7L, false).get(0);

		assertThat(dto.rewardTier()).isZero();
		assertThat(dto.rewardClaimableAt()).isEqualTo(Instant.parse("2026-11-04T12:00:00Z"));
	}
}
