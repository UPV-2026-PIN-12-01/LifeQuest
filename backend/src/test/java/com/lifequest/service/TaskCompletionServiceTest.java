package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
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
import com.lifequest.model.entity.Task;
import com.lifequest.model.entity.TaskSession;
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
	private Task task;
	private Reward reward;

	@BeforeEach
	void setUp() {
		taskRepository = mock(TaskRepository.class);
		taskSessionRepository = mock(TaskSessionRepository.class);
		rewardService = mock(RewardService.class);
		characterStatsService = mock(CharacterStatsService.class);
		task = new Task();
		task.setName("Repasar test-can-delete-later");
		task.setStatus("PENDING");
		task.setPriority("MID");
		task.setColor("purple");
		task.setStartTime(START);
		task.setEndTime(END);
		task.setRewardId(3L);
		reward = new Reward();
		reward.setXpReward(20L);
		when(taskRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(task));
		when(rewardService.getById(3L)).thenReturn(reward);
		when(rewardService.xpByRewardId(anyCollection())).thenReturn(Map.of(3L, 20L));
		when(taskSessionRepository.findByTaskId(any())).thenReturn(Optional.empty());
	}

	@Test
	void completingAfterTheEndGrantsBaseXp() {
		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		verify(rewardService).claim(eq(reward), eq(20L), eq(Instant.parse("2026-10-10T10:30:00Z")));
		verify(characterStatsService).addXp(7L, 20L);
		assertThat(task.getStatus()).isEqualTo("DONE");
	}

	@Test
	void focusedSessionMultipliesXp() {
		withSession("2026-10-10T09:00:00Z", 3600);

		serviceAt("2026-10-10T10:00:30Z").complete(7L, 1L);

		verify(characterStatsService).addXp(7L, 30L);
	}

	@Test
	void checkInCoveringMostOfTheTaskAllowsEarlyCompletion() {
		withSession("2026-10-10T09:00:00Z", 0);

		serviceAt("2026-10-10T09:45:00Z").complete(7L, 1L);

		verify(characterStatsService).addXp(7L, 24L);
	}

	@Test
	void lateCheckOutCountsAsUnverified() {
		withSession("2026-10-10T09:00:00Z", 0);

		serviceAt("2026-10-10T11:00:00Z").complete(7L, 1L);

		verify(characterStatsService).addXp(7L, 20L);
	}

	@Test
	void cannotCompleteEarlyWithoutCheckIn() {
		assertThatThrownBy(() -> serviceAt("2026-10-10T09:30:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);
		verify(characterStatsService, never()).addXp(any(), anyLong());
	}

	@Test
	void cannotAnswerTwiceOrAfterTheWindow() {
		task.setStatus("DONE");
		assertThatThrownBy(() -> serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);

		task.setStatus("PENDING");
		assertThatThrownBy(() -> serviceAt("2026-10-11T10:01:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);
	}

	@Test
	void alreadyClaimedRewardIsNotPaidAgain() {
		reward.setClaimedAt(Instant.parse("2026-10-10T10:10:00Z"));

		assertThatThrownBy(() -> serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L))
				.isInstanceOf(InvalidStateException.class);
		verify(characterStatsService, never()).addXp(any(), anyLong());
	}

	@Test
	void dailyCapHalvesThenStopsXp() {
		when(rewardService.claimedXpBetween(eq(7L), any(), any())).thenReturn(190L);

		serviceAt("2026-10-10T10:30:00Z").complete(7L, 1L);

		// 10 at full rate up to 200, then half of the other 10.
		verify(characterStatsService).addXp(7L, 15L);
		assertThat(TaskCompletionService.capToDailyLimit(30, 0)).isEqualTo(30);
		assertThat(TaskCompletionService.capToDailyLimit(30, 390)).isEqualTo(10);
		assertThat(TaskCompletionService.capToDailyLimit(30, 400)).isZero();
	}

	@Test
	void skipMarksNotDoneWithoutXp() {
		TaskDto dto = serviceAt("2026-10-10T10:30:00Z").skip(7L, 1L);

		assertThat(dto.status()).isEqualTo("not_completed");
		verify(characterStatsService, never()).addXp(any(), anyLong());
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

	private void withSession(String startedAt, long focusedSeconds) {
		TaskSession session = new TaskSession();
		session.setStartedAt(Instant.parse(startedAt));
		session.setLastHeartbeatAt(Instant.parse(startedAt));
		session.setFocusedSeconds(focusedSeconds);
		when(taskSessionRepository.findByTaskId(any())).thenReturn(Optional.of(session));
	}

	private TaskCompletionService serviceAt(String now) {
		Clock clock = Clock.fixed(Instant.parse(now), MADRID);
		TaskService taskService = new TaskService(taskRepository, rewardService, mock(UserService.class),
				taskSessionRepository, clock);
		return new TaskCompletionService(taskService, taskRepository, taskSessionRepository, rewardService,
				characterStatsService, clock);
	}
}
