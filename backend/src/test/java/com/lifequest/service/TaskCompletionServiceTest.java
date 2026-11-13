package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.lifequest.exception.InvalidStateException;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.ActivityType;
import com.lifequest.model.entity.Task;
import com.lifequest.model.entity.TaskSession;
import com.lifequest.model.entity.User;
import com.lifequest.repository.TaskRepository;
import com.lifequest.repository.TaskSessionRepository;

class TaskCompletionServiceTest {

	private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");
	// The task runs 09:00-10:00 UTC on 2026-10-10.
	private static final Instant START = Instant.parse("2026-10-10T09:00:00Z");
	private static final Instant END = Instant.parse("2026-10-10T10:00:00Z");

	private TaskRepository taskRepository;
	private TaskSessionRepository taskSessionRepository;
	private RewardService rewardService;
	private CharacterStatsService characterStatsService;
	private UserService userService;
	private Task task;
	private Reward reward;

	@BeforeEach
	void setUp() {
		taskRepository = mock(TaskRepository.class);
		taskSessionRepository = mock(TaskSessionRepository.class);
		rewardService = mock(RewardService.class);
		characterStatsService = mock(CharacterStatsService.class);
		userService = mock(UserService.class);
		when(userService.getById(7L)).thenReturn(new User());
		task = new Task();
		task.setUserId(7L);
		task.setName("Repasar test-can-delete-later");
		task.setStatus("PENDING");
		task.setActivityType(ActivityType.STUDY);
		task.setPriority("MID");
		task.setColor("purple");
		task.setStartTime(START);
		task.setEndTime(END);
		task.setRewardId(3L);
		reward = new Reward();
		reward.setXpReward(20L);
		reward.setGoldReward(6L);
		when(taskRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(task));
		when(rewardService.getByIdForUpdate(3L)).thenReturn(reward);
		when(rewardService.rewardsById(anyCollection())).thenReturn(Map.of(3L, reward));
		when(taskSessionRepository.findByTaskId(any())).thenReturn(Optional.empty());
		when(characterStatsService.recordTaskCompletion(7L)).thenReturn(1.0);
		when(characterStatsService.recordXpActivityForTask(7L)).thenReturn(1.0);
		when(characterStatsService.addAwardedXp(anyLong(), anyLong()))
				.thenAnswer(invocation -> invocation.getArgument(1));
	}

	@Test
	void completingAfterTheEndGrantsDurationAdjustedXp() {
		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		verify(rewardService).claim(eq(reward), eq(26L), eq(8L), eq(Instant.parse("2026-10-10T10:30:00Z")));
		verify(characterStatsService).addAwardedXp(7L, 26L);
		verify(characterStatsService).addGold(7L, 8L);
		verify(characterStatsService).increaseCharacterStats(7L, 0, 0, 0, 0, 3);
		assertThat(task.getStatus()).isEqualTo("DONE");
	}

	@Test
	void localTestingCompletionCanCompleteFutureTaskWithoutSession() {
		TaskDto completed = serviceAt("2026-10-10T08:00:00Z").completeInstantlyForTesting(7L, 1L);

		assertThat(task.getStatus()).isEqualTo("DONE");
		verify(rewardService).claim(eq(reward), eq(26L), eq(8L), eq(Instant.parse("2026-10-10T08:00:00Z")));
		verify(characterStatsService).addGold(7L, 8L);
		verify(characterStatsService).addAwardedXp(7L, 26L);
		verify(taskRepository).save(task);
		assertThat(completed.status()).isEqualTo("completed");
	}

	@Test
	void taskGoldUsesOnlyDurationBoostAndRoundsTheAward() {
		task.setStartTime(START);
		task.setEndTime(START.plus(Duration.ofMinutes(30)));
		withSession("2026-10-10T09:00:00Z", 1260);

		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		verify(characterStatsService).addGold(7L, 7L);
		verify(rewardService).claim(
				eq(reward), anyLong(), eq(7L), eq(Instant.parse("2026-10-10T10:30:00Z")));
	}

	@Test
	void taskStreakMultiplierIsAppliedOnlyWhenCompletingTasks() {
		when(characterStatsService.recordTaskCompletion(7L)).thenReturn(1.1);

		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		verify(characterStatsService).addAwardedXp(7L, 28L);
		verify(rewardService).claim(eq(reward), eq(28L), eq(8L), eq(Instant.parse("2026-10-10T10:30:00Z")));
	}

	@Test
	void taskAndAccountStreakMultipliersAreBothAppliedToTaskXp() {
		when(characterStatsService.recordTaskCompletion(7L)).thenReturn(1.1);
		when(characterStatsService.recordXpActivityForTask(7L)).thenReturn(1.15);

		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		verify(characterStatsService).addAwardedXp(7L, 32L);
		verify(rewardService).claim(eq(reward), eq(32L), eq(8L), eq(Instant.parse("2026-10-10T10:30:00Z")));
		verify(characterStatsService).addGold(7L, 8L);
	}

	@Test
	void onlyCompletedTasksGrantActivityStats() {
		task.setActivityType(ActivityType.EXERCISE);

		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		verify(characterStatsService).increaseCharacterStats(7L, 0, 2, 0, 1, 0);
	}

	@Test
	void legacyUndefinedActivityCanBeCompletedWithoutStatBonus() {
		task.setActivityType(ActivityType.UNDEFINED);

		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		verify(characterStatsService, never()).increaseCharacterStats(anyLong(), anyLong(), anyInt(), anyInt(),
				anyInt(), anyInt());
		assertThat(task.getStatus()).isEqualTo("DONE");
	}

	@Test
	void focusedSessionMultipliesXp() {
		withSession("2026-10-10T09:00:00Z", 3600);

		serviceAt("2026-10-10T10:00:30Z").complete(7L, 1L);

		verify(characterStatsService).addAwardedXp(7L, 38L);
	}

	@Test
	void checkInCoveringMostOfTheTaskAllowsEarlyCompletion() {
		withSession("2026-10-10T09:00:00Z", 0);

		serviceAt("2026-10-10T09:45:00Z").complete(7L, 1L);

		verify(characterStatsService).addAwardedXp(7L, 31L);
	}

	@Test
	void lateCheckOutCountsAsUnverified() {
		withSession("2026-10-10T09:00:00Z", 0);

		serviceAt("2026-10-10T11:00:00Z").complete(7L, 1L);

		verify(characterStatsService).addAwardedXp(7L, 26L);
	}

	@Test
	void cannotCompleteEarlyWithoutCheckIn() {
		assertThatThrownBy(() -> serviceAt("2026-10-10T09:30:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);
		verify(characterStatsService, never()).addAwardedXp(any(), anyLong());
		verify(characterStatsService, never()).increaseCharacterStats(anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt());
	}

	@Test
	void cannotAnswerTwiceOrAfterTheWindow() {
		task.setStatus("DONE");
		assertThatThrownBy(() -> serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);
		verify(characterStatsService, never()).increaseCharacterStats(anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt());

		task.setStatus("PENDING");
		assertThatThrownBy(() -> serviceAt("2026-10-11T10:01:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);
	}

	@Test
	void alreadyClaimedRewardIsNotPaidAgain() {
		reward.setClaimedAt(Instant.parse("2026-10-10T10:10:00Z"));

		assertThatThrownBy(() -> serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);
		verify(characterStatsService, never()).addAwardedXp(any(), anyLong());
	}

	@Test
	void dailyCapHalvesThenStopsXp() {
		when(rewardService.claimedXpForSourceBetween(eq(7L), eq(com.lifequest.model.entity.RewardSource.TASK),
				any(), any())).thenReturn(190L);

		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		// 10 at full rate up to 200, then half of the other 10.
		verify(characterStatsService).addAwardedXp(7L, 18L);
		assertThat(TaskCompletionService.capToDailyLimit(30, 0)).isEqualTo(30);
		assertThat(TaskCompletionService.capToDailyLimit(30, 390)).isEqualTo(10);
		assertThat(TaskCompletionService.capToDailyLimit(30, 400)).isZero();
	}

	@Test
	void skipMarksNotDoneWithoutGrantingXpOrStats() {
		TaskDto dto = serviceAt("2026-10-10T10:30:00Z").skip(7L, 1L);

		assertThat(dto.status()).isEqualTo("not_completed");
		verify(characterStatsService, never()).addAwardedXp(any(), anyLong());
		verify(characterStatsService).recordTaskMiss(7L);
		verify(characterStatsService, never()).increaseCharacterStats(anyLong(), anyLong(), anyInt(), anyInt(),
				anyInt(), anyInt());
	}

	@Test
	void checkInOpensShortlyBeforeTheStart() {
		assertThatThrownBy(() -> serviceAt("2026-10-10T08:50:00Z").start(7L, 1L))
				.isInstanceOf(InvalidStateException.class);

		serviceAt("2026-10-10T08:56:00Z").start(7L, 1L);

		ArgumentCaptor<TaskSession> saved = ArgumentCaptor.forClass(TaskSession.class);
		verify(taskSessionRepository).save(saved.capture());
		assertThat(saved.getValue().getStartedAt()).isEqualTo(Instant.parse("2026-10-10T08:56:00Z"));
		assertThat(saved.getValue().getUserId()).isEqualTo(7L);
	}

	@Test
	void onlyShortHeartbeatGapsInsideTheTaskCountAsFocus() {
		TaskSession session = new TaskSession();
		session.setStartedAt(Instant.parse("2026-10-10T08:58:00Z"));
		session.setLastHeartbeatAt(Instant.parse("2026-10-10T08:59:30Z"));

		// 60 s gap, but only the 30 s after 09:00 count.
		TaskVerification.recordHeartbeat(task, session, Instant.parse("2026-10-10T09:00:30Z"));
		assertThat(session.getFocusedSeconds()).isEqualTo(30);

		// A 5-minute gap means the page was hidden: nothing is added.
		TaskVerification.recordHeartbeat(task, session, Instant.parse("2026-10-10T09:05:30Z"));
		assertThat(session.getFocusedSeconds()).isEqualTo(30);
		assertThat(session.getLastHeartbeatAt()).isEqualTo(Instant.parse("2026-10-10T09:05:30Z"));
	}

	@Test
	void durationMultiplierStartsAtThirtyMinutesAndCapsAtThreeHours() {
		assertThat(TaskDurationReward.multiplier(Duration.ofMinutes(15))).isEqualTo(1.0);
		assertThat(TaskDurationReward.multiplier(Duration.ofMinutes(30))).isCloseTo(1.1, within(0.000001));
		assertThat(TaskDurationReward.multiplier(Duration.ofHours(1))).isCloseTo(1.28, within(0.000001));
		assertThat(TaskDurationReward.multiplier(Duration.ofHours(3))).isEqualTo(2.0);
		assertThat(TaskDurationReward.multiplier(Duration.ofHours(4))).isEqualTo(2.0);
	}

	private void withSession(String startedAt, long focusedSeconds) {
		TaskSession session = new TaskSession();
		session.setStartedAt(Instant.parse(startedAt));
		session.setLastHeartbeatAt(Instant.parse(startedAt));
		session.setFocusedSeconds(focusedSeconds);
		when(taskSessionRepository.findByTaskId(any())).thenReturn(Optional.of(session));
	}

	private TaskCompletionService serviceAt(String now) {
		Clock clock = Clock.fixed(Instant.parse(now), MADRID);
		TaskService taskService = new TaskService(taskRepository, rewardService, userService,
				taskSessionRepository, clock);
		return new TaskCompletionService(taskService, taskRepository, taskSessionRepository, rewardService,
				characterStatsService, clock);
	}
}
