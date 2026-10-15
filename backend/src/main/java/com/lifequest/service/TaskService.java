package com.lifequest.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.Task;
import com.lifequest.repository.TaskRepository;

@Service
public class TaskService {

	static final long MAX_RANGE_DAYS = 62;
	// Values of the Supabase enums public.task_status and public.task_priority.
	static final String DB_STATUS_DONE = "DONE";
	static final String DB_STATUS_NOT_DONE = "NOT_DONE";
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	private final TaskRepository taskRepository;
	private final RewardService rewardService;
	private final UserService userService;
	private final Clock clock;

	public TaskService(TaskRepository taskRepository, RewardService rewardService, UserService userService, Clock clock) {
		this.taskRepository = taskRepository;
		this.rewardService = rewardService;
		this.userService = userService;
		this.clock = clock;
	}

	// Tasks whose start falls on any day from `from` to `to`, both included, in the app time zone.
	@Transactional(readOnly = true)
	public List<TaskDto> findForUser(Long userId, LocalDate from, LocalDate to) {
		if (from.isAfter(to)) {
			throw new InvalidRequestException("from must be on or before to");
		}
		if (ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
			throw new InvalidRequestException("Date range must be " + MAX_RANGE_DAYS + " days or fewer");
		}
		userService.getById(userId);

		List<Task> tasks = findTasks(userId, from, to);
		Map<Long, Long> xpByReward = rewardService.xpByRewardId(
				tasks.stream().map(Task::getRewardId).filter(Objects::nonNull).distinct().toList());
		Instant now = clock.instant();
		return tasks.stream().map(task -> toDto(task, xpByReward, now)).toList();
	}

	@Transactional(readOnly = true)
	public long countCompletedToday(Long userId) {
		LocalDate today = LocalDate.now(clock);
		return findTasks(userId, today, today).stream()
				.filter(task -> DB_STATUS_DONE.equals(task.getStatus()))
				.count();
	}

	private List<Task> findTasks(Long userId, LocalDate from, LocalDate to) {
		ZoneId zone = clock.getZone();
		Instant start = from.atStartOfDay(zone).toInstant();
		Instant endExclusive = to.plusDays(1).atStartOfDay(zone).toInstant();
		return taskRepository.findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
				userId, start, endExclusive);
	}

	private TaskDto toDto(Task task, Map<Long, Long> xpByReward, Instant now) {
		ZoneId zone = clock.getZone();
		ZonedDateTime start = task.getStartTime().atZone(zone);
		ZonedDateTime end = task.getEndTime().atZone(zone);
		long xp = task.getRewardId() == null ? 0 : xpByReward.getOrDefault(task.getRewardId(), 0L);
		return new TaskDto(
				task.getId(),
				task.getName(),
				task.getIcon(),
				task.getColor(),
				start.toLocalDate().toString(),
				start.format(TIME),
				end.format(TIME),
				displayStatus(task, now),
				displayPriority(task.getPriority()),
				xp);
	}

	// DONE / NOT_DONE are the user's outcome; any other stored value shows as pending, in progress or
	// finished depending on the clock, so the card can ask "¿La completaste?".
	static String displayStatus(Task task, Instant now) {
		if (DB_STATUS_DONE.equals(task.getStatus())) {
			return "completed";
		}
		if (DB_STATUS_NOT_DONE.equals(task.getStatus())) {
			return "not_completed";
		}
		if (now.isBefore(task.getStartTime())) {
			return "pending";
		}
		return now.isBefore(task.getEndTime()) ? "in_progress" : "finished";
	}

	// The home shows three levels: MAX and HIGH are high, MID is medium, LOW and NULL are low.
	static String displayPriority(String priority) {
		if ("MAX".equals(priority) || "HIGH".equals(priority)) {
			return "high";
		}
		return "MID".equals(priority) ? "medium" : "low";
	}
}
