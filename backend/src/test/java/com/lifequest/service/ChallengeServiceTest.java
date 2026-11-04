package com.lifequest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

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
		Task task = new Task();
		Reward reward = new Reward();
		reward.setXpReward(25L);
		reward.setGoldReward(10L);
		TaskDto taskDto = new TaskDto(
				1L, "Leer", "", "📘", "blue", "2026-11-05", "10:00", "11:00",
				"pending", "low", 10L, ActivityType.STUDY, "none", null, false, 0);
		when(taskService.findOwned(7L, 1L)).thenReturn(task);
		when(taskService.describe(task)).thenReturn(taskDto);
		when(rewardService.create(7L, 25L, 10L, RewardSource.CHALLENGE)).thenReturn(9L);
		when(rewardService.getById(9L)).thenReturn(reward);
		when(challengeRepository.save(any(Challenge.class)))
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
}
