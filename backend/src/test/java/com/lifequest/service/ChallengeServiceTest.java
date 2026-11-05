package com.lifequest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lifequest.exception.InvalidStateException;
import com.lifequest.model.dto.ChallengeRequestDto;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.ActivityType;
import com.lifequest.model.entity.Challenge;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.RewardSource;
import com.lifequest.model.entity.Task;
import com.lifequest.repository.ChallengeRepository;

class ChallengeServiceTest {

	@Test
	void createsChallengeRewardWithChallengeSource() {
		ChallengeRepository challengeRepository = mock(ChallengeRepository.class);
		TaskService taskService = mock(TaskService.class);
		RewardService rewardService = mock(RewardService.class);
		CharacterStatsService characterStatsService = mock(CharacterStatsService.class);
		UserService userService = mock(UserService.class);
		Task task = mock(Task.class);
		when(task.getId()).thenReturn(1L);
		Reward reward = new Reward();
		reward.setXpReward(25L);
		reward.setGoldReward(10L);
		TaskDto taskDto = new TaskDto(
				1L, "Leer", "", "📘", "blue", "2026-11-05", "10:00", "11:00",
				"pending", "low", 10L, 5L, ActivityType.STUDY, "none", null, false, 0);
		when(taskService.findOwned(7L, 1L)).thenReturn(task);
		when(taskService.describe(task)).thenReturn(taskDto);
		when(rewardService.create(7L, 25L, 10L, RewardSource.CHALLENGE)).thenReturn(9L);
		when(rewardService.getById(9L)).thenReturn(reward);
		when(challengeRepository.saveAndFlush(any(Challenge.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		ChallengeService service = new ChallengeService(
				challengeRepository,
				taskService,
				rewardService,
				characterStatsService,
				userService,
				Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC));

		service.create(7L, new ChallengeRequestDto("Leer", "", "EASY", 1L, ""));

		verify(rewardService).create(7L, 25L, 10L, RewardSource.CHALLENGE);
	}

	@Test
	void rejectsTasksAlreadyAssignedToAChallenge() {
		ChallengeRepository challengeRepository = mock(ChallengeRepository.class);
		TaskService taskService = mock(TaskService.class);
		RewardService rewardService = mock(RewardService.class);
		UserService userService = mock(UserService.class);
		Task task = mock(Task.class);
		when(task.getId()).thenReturn(1L);
		when(taskService.findOwned(7L, 1L)).thenReturn(task);
		when(challengeRepository.existsByTaskId(1L)).thenReturn(true);
		ChallengeService service = new ChallengeService(
				challengeRepository, taskService, rewardService, mock(CharacterStatsService.class),
				userService, Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC));

		assertThrows(InvalidStateException.class,
				() -> service.create(7L, new ChallengeRequestDto("Leer", "", "EASY", 1L, "")));

		verify(rewardService, never()).create(7L, 25L, 10L, RewardSource.CHALLENGE);
	}

	@Test
	void awardsXpAndGoldWhenClaimingAChallengeReward() {
		ChallengeRepository challengeRepository = mock(ChallengeRepository.class);
		TaskService taskService = mock(TaskService.class);
		RewardService rewardService = mock(RewardService.class);
		CharacterStatsService characterStatsService = mock(CharacterStatsService.class);
		UserService userService = mock(UserService.class);
		Challenge challenge = new Challenge();
		challenge.setUserId(7L);
		challenge.setRewardId(9L);
		challenge.setCompleted(true);
		challenge.setRating((short) 8);
		Reward reward = new Reward();
		reward.setXpReward(100L);
		reward.setGoldReward(50L);
		when(challengeRepository.findByIdAndUserId(4L, 7L)).thenReturn(Optional.of(challenge));
		when(rewardService.getByIdForUpdate(9L)).thenReturn(reward);
		when(rewardService.getById(9L)).thenReturn(reward);
		when(characterStatsService.addXp(7L, 100L)).thenReturn(115L);
		Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);
		ChallengeService service = new ChallengeService(
				challengeRepository, taskService, rewardService, characterStatsService, userService, clock);

		service.claim(7L, 4L);

		verify(rewardService).claim(reward, 115L, clock.instant());
		verify(characterStatsService).addGold(7L, 50L);
	}
}
