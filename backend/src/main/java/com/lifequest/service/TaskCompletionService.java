package com.lifequest.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidStateException;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.Task;
import com.lifequest.model.entity.TaskSession;
import com.lifequest.repository.TaskRepository;
import com.lifequest.repository.TaskSessionRepository;
import com.lifequest.model.entity.ActivityType;

// Check-in, heartbeats and the final answer of a task. The client only says "started", "still here" and
// "done"; every time and every XP amount comes from the server clock and the stored task.
@Service
public class TaskCompletionService {

	// The check-in opens a little before the planned start.
	static final Duration EARLY_CHECKIN = Duration.ofMinutes(5);
	// XP granted per day at full rate, then at half rate up to the daily maximum.
	static final long DAILY_FULL_RATE_XP = 200;
	static final long DAILY_MAX_XP = 400;

	private final TaskService taskService;
	private final TaskRepository taskRepository;
	private final TaskSessionRepository taskSessionRepository;
	private final RewardService rewardService;
	private final CharacterStatsService characterStatsService;
	private final Clock clock;

	public TaskCompletionService(TaskService taskService, TaskRepository taskRepository,
			TaskSessionRepository taskSessionRepository, RewardService rewardService,
			CharacterStatsService characterStatsService, Clock clock) {
		this.taskService = taskService;
		this.taskRepository = taskRepository;
		this.taskSessionRepository = taskSessionRepository;
		this.rewardService = rewardService;
		this.characterStatsService = characterStatsService;
		this.clock = clock;
	}

	// Repeating it while the session is open returns the same session.
	@Transactional
	public TaskDto start(Long userId, Long taskId) {
		Task task = pendingTask(userId, taskId);
		Instant now = clock.instant();
		TaskSession session = taskSessionRepository.findByTaskId(taskId).orElse(null);
		if (session != null) {
			if (!session.isOpen()) {
				throw new InvalidStateException("Task was already checked out");
			}
			return taskService.describe(task);
		}
		if (now.isBefore(task.getStartTime().minus(EARLY_CHECKIN))) {
			throw new InvalidStateException("Task has not started yet");
		}
		if (!now.isBefore(task.getEndTime())) {
			throw new InvalidStateException("Task has already ended");
		}
		session = new TaskSession();
		session.setTaskId(taskId);
		session.setUserId(userId);
		session.setStartedAt(now);
		session.setLastHeartbeatAt(now);
		taskSessionRepository.save(session);
		return taskService.describe(task);
	}

	// Sent about every minute while the page is visible.
	@Transactional
	public TaskDto heartbeat(Long userId, Long taskId) {
		Task task = pendingTask(userId, taskId);
		TaskSession session = openSession(taskId);
		TaskVerification.recordHeartbeat(task, session, clock.instant());
		taskSessionRepository.save(session);
		return taskService.describe(task);
	}

	// Early completion needs a check-in covering most of the task; otherwise only after the planned end.
	@Transactional
	public TaskDto complete(Long userId, Long taskId) {
		Task task = pendingTask(userId, taskId);
		Instant now = clock.instant();
		TaskSession session = closeSession(task, now);
		TaskVerification verification = TaskVerification.of(task, session, now);
		if (now.isBefore(task.getEndTime()) && verification == TaskVerification.NONE) {
			throw new InvalidStateException("Task can be completed only after it ends");
		}
		if (task.getRewardId() != null) {
			Reward reward = rewardService.getById(task.getRewardId());
			if (reward.getClaimedAt() != null) {
				throw new InvalidStateException("Reward was already claimed");
			}
			long granted = capToDailyLimit(verification.apply(reward.getXpReward()), claimedToday(userId, now));
			rewardService.claim(reward, granted, now);
			if (granted > 0) {
				characterStatsService.addXp(userId, granted);
			}
		}
		increaseCharacterStats(userId, task);
		task.setStatus(TaskService.DB_STATUS_DONE);
		taskRepository.save(task);
		return taskService.describe(task);
	}

	@Transactional
	public TaskDto skip(Long userId, Long taskId) {
		Task task = pendingTask(userId, taskId);
		closeSession(task, clock.instant());
		increaseCharacterStats(userId, task);
		task.setStatus(TaskService.DB_STATUS_NOT_DONE);
		taskRepository.save(task);
		return taskService.describe(task);
	}

	// Full rate until DAILY_FULL_RATE_XP granted today, half rate after it, nothing past DAILY_MAX_XP.
	static long capToDailyLimit(long xp, long grantedToday) {
		long fullRate = Math.max(0, Math.min(xp, DAILY_FULL_RATE_XP - grantedToday));
		long halfRoom = Math.max(0, DAILY_MAX_XP - Math.max(grantedToday, DAILY_FULL_RATE_XP));
		long halfRate = Math.min((xp - fullRate) / 2, halfRoom);
		return fullRate + halfRate;
	}

	private long claimedToday(Long userId, Instant now) {
		ZoneId zone = clock.getZone();
		LocalDate today = LocalDate.ofInstant(now, zone);
		return rewardService.claimedXpBetween(userId, today.atStartOfDay(zone).toInstant(),
				today.plusDays(1).atStartOfDay(zone).toInstant());
	}

	// Only a pending task inside its confirm window can change.
	private Task pendingTask(Long userId, Long taskId) {
		Task task = taskService.findOwned(userId, taskId);
		if (TaskService.DB_STATUS_DONE.equals(task.getStatus()) || TaskService.DB_STATUS_NOT_DONE.equals(task.getStatus())) {
			throw new InvalidStateException("Task was already answered");
		}
		if (TaskService.isConfirmWindowClosed(task, clock.instant())) {
			throw new InvalidStateException("Task can no longer be confirmed");
		}
		return task;
	}

	private TaskSession openSession(Long taskId) {
		return taskSessionRepository.findByTaskId(taskId)
				.filter(TaskSession::isOpen)
				.orElseThrow(() -> new InvalidStateException("Task has no open session"));
	}

	// The last stretch since the previous heartbeat counts if it was short, then the session ends.
	private TaskSession closeSession(Task task, Instant now) {
		TaskSession session = taskSessionRepository.findByTaskId(task.getId()).orElse(null);
		if (session != null && session.isOpen()) {
			TaskVerification.recordHeartbeat(task, session, now);
			session.setEndedAt(now);
			taskSessionRepository.save(session);
		}
		return session;
	}

	private void increaseCharacterStats(long userId, Task task) {
		ActivityType activityType = task.getActivityType();
		if (activityType == null) {
			throw new InvalidStateException("Task has no activity type");
		}
		switch (activityType) {
			case EXERCISE:
				characterStatsService.increaseCharacterStats(userId, 0, 2, 0, 1, 0);
				break;
			case STUDY:
				characterStatsService.increaseCharacterStats(userId, 0, 0, 0, 0, 3);
				break;
			case SOCIAL:
				characterStatsService.increaseCharacterStats(userId, 2, 0, 1, 0, 0);
				break;
			case SPORTS:
				characterStatsService.increaseCharacterStats(userId, 0, 0, 1, 2, 0);
				break;
			case ENTERTAINMENT:
				characterStatsService.increaseCharacterStats(userId, 0, 0, 2, 0, 0);
				break;
			case WORK:
				characterStatsService.increaseCharacterStats(userId, 1, 1, 0, 0, 1);
				break;
			case UNDEFINED:
				break;
			default:
				throw new InvalidStateException("Unknown activity type");
		}
	}
}
