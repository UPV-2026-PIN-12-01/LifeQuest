package com.lifequest.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.dto.TaskRequestDto;
import com.lifequest.exception.InvalidStateException;
import com.lifequest.model.entity.Task;
import com.lifequest.model.entity.TaskSession;
import com.lifequest.repository.TaskRepository;
import com.lifequest.repository.TaskSessionRepository;

@Service
public class TaskService {

	static final long MAX_RANGE_DAYS = 62;
	// Values of the Supabase enums public.task_status and public.task_priority.
	static final String DB_STATUS_DONE = "DONE";
	static final String DB_STATUS_NOT_DONE = "NOT_DONE";
	static final String DB_STATUS_PENDING = "PENDING";
	static final int MAX_TITLE_LENGTH = 80;
	static final String DEFAULT_ICON = "📆";
	// Same values as the chk_task_color constraint.
	static final Set<String> COLORS = Set.of("purple", "red", "blue", "yellow", "green");
	// XP of the reward created with each task, by form priority.
	static final Map<String, Long> XP_BY_PRIORITY = Map.of("low", 10L, "medium", 20L, "high", 30L);
	static final Map<String, String> DB_PRIORITY_BY_FORM = Map.of("low", "LOW", "medium", "MID", "high", "HIGH");
	// A task may be planned a little late (the form was slow), but not backdated to claim XP.
	static final Duration PAST_START_GRACE = Duration.ofMinutes(15);
	// Shorter tasks would let users farm XP with many one-minute tasks.
	static final Duration MIN_DURATION = Duration.ofMinutes(15);
	// After this, an unconfirmed task counts as not done and can no longer be completed.
	static final Duration CONFIRM_WINDOW = Duration.ofHours(24);
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	private final TaskRepository taskRepository;
	private final RewardService rewardService;
	private final UserService userService;
	private final TaskSessionRepository taskSessionRepository;
	private final Clock clock;

	public TaskService(TaskRepository taskRepository, RewardService rewardService, UserService userService,
			TaskSessionRepository taskSessionRepository, Clock clock) {
		this.taskRepository = taskRepository;
		this.rewardService = rewardService;
		this.userService = userService;
		this.taskSessionRepository = taskSessionRepository;
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
		Map<Long, TaskSession> sessionByTask = tasks.isEmpty() ? Map.of()
				: taskSessionRepository.findByTaskIdIn(tasks.stream().map(Task::getId).toList()).stream()
						.collect(Collectors.toMap(TaskSession::getTaskId, Function.identity()));
		Instant now = clock.instant();
		return tasks.stream().map(task -> toDto(task, xpByReward, sessionByTask.get(task.getId()), now)).toList();
	}

	@Transactional(readOnly = true)
	public long countCompletedToday(Long userId) {
		LocalDate today = LocalDate.now(clock);
		return findTasks(userId, today, today).stream()
				.filter(task -> DB_STATUS_DONE.equals(task.getStatus()))
				.count();
	}

	@Transactional
	public TaskDto create(Long userId, TaskRequestDto request) {
		userService.getById(userId);
		Task task = new Task();
		task.setUserId(userId);
		task.setStatus(DB_STATUS_PENDING);
		apply(task, request);
		long xp = XP_BY_PRIORITY.get(request.priority());
		task.setRewardId(rewardService.create(userId, xp));
		return toDto(taskRepository.save(task), xp);
	}

	// Status is not part of the form, so editing keeps it; the reward XP follows the new priority.
	// Once started a task is frozen, so its priority or times cannot be raised after the fact.
	@Transactional
	public TaskDto update(Long userId, Long taskId, TaskRequestDto request) {
		Task task = findOwned(userId, taskId);
		if (!clock.instant().isBefore(task.getStartTime())) {
			throw new InvalidStateException("Task cannot be edited once it has started");
		}
		apply(task, request);
		long xp = XP_BY_PRIORITY.get(request.priority());
		if (task.getRewardId() == null) {
			task.setRewardId(rewardService.create(userId, xp));
		} else {
			rewardService.updateXp(task.getRewardId(), xp);
		}
		return toDto(taskRepository.save(task), xp);
	}

	// Deletes the row for real, and its reward unless another task still points to it.
	@Transactional
	public void delete(Long userId, Long taskId) {
		Task task = findOwned(userId, taskId);
		Long rewardId = task.getRewardId();
		taskRepository.delete(task);
		taskRepository.flush();
		if (rewardId != null && !taskRepository.existsByRewardId(rewardId)) {
			rewardService.delete(rewardId);
		}
	}

	Task findOwned(Long userId, Long taskId) {
		return taskRepository.findByIdAndUserId(taskId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));
	}

	private void apply(Task task, TaskRequestDto request) {
		if (request == null) {
			throw new InvalidRequestException("Task is required");
		}
		String title = request.title() == null ? "" : request.title().trim();
		if (title.isEmpty() || title.length() > MAX_TITLE_LENGTH) {
			throw new InvalidRequestException("title must be 1 to " + MAX_TITLE_LENGTH + " characters");
		}
		if (request.color() == null || !COLORS.contains(request.color())) {
			throw new InvalidRequestException("color must be one of " + COLORS);
		}
		if (request.priority() == null || !DB_PRIORITY_BY_FORM.containsKey(request.priority())) {
			throw new InvalidRequestException("priority must be low, medium or high");
		}
		if (request.date() == null || request.startTime() == null || request.endTime() == null) {
			throw new InvalidRequestException("date, startTime and endTime are required");
		}
		if (!request.endTime().isAfter(request.startTime())) {
			throw new InvalidRequestException("endTime must be after startTime");
		}
		Instant start = toInstant(request.date(), request.startTime());
		Instant end = toInstant(request.date(), request.endTime());
		if (start.isBefore(clock.instant().minus(PAST_START_GRACE))) {
			throw new InvalidRequestException("Task cannot start in the past");
		}
		if (Duration.between(start, end).compareTo(MIN_DURATION) < 0) {
			throw new InvalidRequestException("Task must last at least " + MIN_DURATION.toMinutes() + " minutes");
		}
		// The day_task_check constraint compares the day of month in UTC, not in the app time zone.
		if (start.atOffset(ZoneOffset.UTC).getDayOfMonth() != end.atOffset(ZoneOffset.UTC).getDayOfMonth()) {
			throw new InvalidRequestException("Task cannot cross midnight UTC");
		}
		String icon = request.icon() == null ? "" : request.icon().trim();
		task.setName(title);
		task.setDescription(request.description() == null ? "" : request.description().trim());
		task.setIcon(icon.isEmpty() ? DEFAULT_ICON : icon);
		task.setColor(request.color());
		task.setPriority(DB_PRIORITY_BY_FORM.get(request.priority()));
		task.setStartTime(start);
		task.setEndTime(end);
	}

	private Instant toInstant(LocalDate date, LocalTime time) {
		return date.atTime(time).atZone(clock.getZone()).toInstant();
	}

	private TaskDto toDto(Task task, long xp) {
		return toDto(task, task.getRewardId() == null ? Map.of() : Map.of(task.getRewardId(), xp), null, clock.instant());
	}

	// The calendar shape of one task with its current reward and session, for the completion endpoints.
	TaskDto describe(Task task) {
		Map<Long, Long> xpByReward = task.getRewardId() == null ? Map.of()
				: rewardService.xpByRewardId(List.of(task.getRewardId()));
		TaskSession session = taskSessionRepository.findByTaskId(task.getId()).orElse(null);
		return toDto(task, xpByReward, session, clock.instant());
	}

	private List<Task> findTasks(Long userId, LocalDate from, LocalDate to) {
		ZoneId zone = clock.getZone();
		Instant start = from.atStartOfDay(zone).toInstant();
		Instant endExclusive = to.plusDays(1).atStartOfDay(zone).toInstant();
		return taskRepository.findByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
				userId, start, endExclusive);
	}

	private TaskDto toDto(Task task, Map<Long, Long> xpByReward, TaskSession session, Instant now) {
		ZoneId zone = clock.getZone();
		ZonedDateTime start = task.getStartTime().atZone(zone);
		ZonedDateTime end = task.getEndTime().atZone(zone);
		long xp = task.getRewardId() == null ? 0 : xpByReward.getOrDefault(task.getRewardId(), 0L);
		return new TaskDto(
				task.getId(),
				task.getName(),
				task.getDescription(),
				task.getIcon(),
				task.getColor(),
				start.toLocalDate().toString(),
				start.format(TIME),
				end.format(TIME),
				displayStatus(task, now),
				displayPriority(task.getPriority()),
				xp,
				TaskVerification.of(task, session, now).id(),
				session == null ? null : session.getStartedAt().toString(),
				session != null && session.isOpen(),
				session == null ? 0 : session.getFocusedSeconds());
	}

	// DONE / NOT_DONE are the user's outcome; any other stored value shows as pending, in progress or
	// finished depending on the clock, so the card can ask "¿La completaste?". Past the confirm window
	// an unanswered task shows as not completed.
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
		if (now.isBefore(task.getEndTime())) {
			return "in_progress";
		}
		return isConfirmWindowClosed(task, now) ? "not_completed" : "finished";
	}

	static boolean isConfirmWindowClosed(Task task, Instant now) {
		return now.isAfter(task.getEndTime().plus(CONFIRM_WINDOW));
	}

	// The home shows three levels: MAX and HIGH are high, MID is medium, LOW and NULL are low.
	static String displayPriority(String priority) {
		if ("MAX".equals(priority) || "HIGH".equals(priority)) {
			return "high";
		}
		return "MID".equals(priority) ? "medium" : "low";
	}
}
