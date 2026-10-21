package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.Task;
import com.lifequest.repository.TaskRepository;

class TaskServiceTest {

	private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");
	// Saturday 2026-10-10 12:00 in Madrid (UTC+2)
	private static final Instant NOW = Instant.parse("2026-10-10T10:00:00Z");

	private TaskRepository taskRepository;
	private RewardService rewardService;
	private TaskService taskService;

	@BeforeEach
	void setUp() {
		taskRepository = mock(TaskRepository.class);
		rewardService = mock(RewardService.class);
		UserService userService = mock(UserService.class);
		taskService = new TaskService(taskRepository, rewardService, userService, Clock.fixed(NOW, MADRID));
	}

	@Test
	void weekRangeUsesLocalMidnights() {
		taskService.findForUser(7L, LocalDate.parse("2026-10-05"), LocalDate.parse("2026-10-11"));

		verify(taskRepository).findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
				7L, Instant.parse("2026-10-04T22:00:00Z"), Instant.parse("2026-10-11T22:00:00Z"));
	}

	@Test
	void mapsTaskToCalendarShapeInLocalTime() {
		Task task = task("PENDING", "2026-10-10T22:30:00Z", "2026-10-10T23:00:00Z", 3L);
		when(taskRepository.findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
				eq(7L), any(), any()))
				.thenReturn(List.of(task));
		when(rewardService.xpByRewardId(anyCollection())).thenReturn(Map.of(3L, 50L));

		List<TaskDto> tasks = taskService.findForUser(7L, LocalDate.parse("2026-10-05"), LocalDate.parse("2026-10-11"));

		TaskDto dto = tasks.get(0);
		assertThat(dto.title()).isEqualTo("Repasar test-can-delete-later");
		assertThat(dto.description()).isEqualTo("test-can-delete-later");
		assertThat(dto.date()).isEqualTo("2026-10-11");
		assertThat(dto.startTime()).isEqualTo("00:30");
		assertThat(dto.endTime()).isEqualTo("01:00");
		assertThat(dto.status()).isEqualTo("pending");
		assertThat(dto.priority()).isEqualTo("medium");
		assertThat(dto.xp()).isEqualTo(50L);
	}

	@Test
	void pendingStatusFollowsTheClock() {
		assertThat(TaskService.displayStatus(task("PENDING", "2026-10-10T09:00:00Z", "2026-10-10T11:00:00Z", null), NOW))
				.isEqualTo("in_progress");
		assertThat(TaskService.displayStatus(task("IN_PROGRESS", "2026-10-10T07:00:00Z", "2026-10-10T08:00:00Z", null), NOW))
				.isEqualTo("finished");
		assertThat(TaskService.displayStatus(task("DONE", "2026-10-10T07:00:00Z", "2026-10-10T08:00:00Z", null), NOW))
				.isEqualTo("completed");
		assertThat(TaskService.displayStatus(task("NOT_DONE", "2026-10-10T07:00:00Z", "2026-10-10T08:00:00Z", null), NOW))
				.isEqualTo("not_completed");
	}

	@Test
	void prioritiesCollapseToThreeLevels() {
		assertThat(TaskService.displayPriority("MAX")).isEqualTo("high");
		assertThat(TaskService.displayPriority("HIGH")).isEqualTo("high");
		assertThat(TaskService.displayPriority("MID")).isEqualTo("medium");
		assertThat(TaskService.displayPriority("LOW")).isEqualTo("low");
		assertThat(TaskService.displayPriority("NULL")).isEqualTo("low");
	}

	@Test
	void rejectsReversedOrTooLongRanges() {
		assertThatThrownBy(() -> taskService.findForUser(7L, LocalDate.parse("2026-10-11"), LocalDate.parse("2026-10-05")))
				.isInstanceOf(InvalidRequestException.class);
		assertThatThrownBy(() -> taskService.findForUser(7L, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")))
				.isInstanceOf(InvalidRequestException.class);
	}

	private static Task task(String status, String start, String end, Long rewardId) {
		Task task = new Task();
		task.setName("Repasar test-can-delete-later");
		task.setDescription("test-can-delete-later");
		task.setIcon("📘");
		task.setStatus(status);
		task.setPriority("MID");
		task.setColor("purple");
		task.setStartTime(Instant.parse(start));
		task.setEndTime(Instant.parse(end));
		task.setRewardId(rewardId);
		return task;
	}
}
