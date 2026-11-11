package com.lifequest.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.entity.User;
import com.lifequest.repository.UserRepository;

@Service
public class UserStreakService {
	private static final int DAY_STREAK_BOOST_CAP = 7;
	private static final int TASK_STREAK_BOOST_CAP = 10;
	private static final double BOOST_PER_STREAK = 0.05;

	private final UserRepository userRepository;
	private final Clock clock;

	public UserStreakService(UserRepository userRepository, Clock clock) {
		this.userRepository = userRepository;
		this.clock = clock;
	}

	@Transactional
	public double recordXpActivity(Long userId) {
		User user = lockUser(userId);
		LocalDate today = LocalDate.now(clock);
		int dayStreak = dayStreakForActivity(user, today);
		if (user.getLastActivityDate() == null || today.isAfter(user.getLastActivityDate())) {
			user.setLastActivityDate(today);
		}

		user.setDayStreak(dayStreak);
		double accountBoost = synchronizeAccountBoost(user);
		userRepository.save(user);
		return accountBoost;
	}

	@Transactional
	public double recordTaskCompletion(Long userId) {
		User user = lockUser(userId);
		int completedTasks = valueOrZero(user.getTaskStreak());
		user.setTaskStreak(increment(completedTasks));
		synchronizeAccountBoost(user);
		userRepository.save(user);
		return multiplier(completedTasks, TASK_STREAK_BOOST_CAP);
	}

	@Transactional
	public void recordTaskMiss(Long userId) {
		User user = lockUser(userId);
		user.setTaskStreak(0);
		synchronizeAccountBoost(user);
		userRepository.save(user);
	}

	@Transactional
	public double synchronizeAccountBoost(Long userId) {
		User user = lockUser(userId);
		double accountBoost = synchronizeAccountBoost(user);
		userRepository.save(user);
		return accountBoost;
	}

	double synchronizeAccountBoost(User user) {
		double accountBoost = multiplier(valueOrZero(user.getDayStreak()), DAY_STREAK_BOOST_CAP);
		user.setXpAccBoost((float) accountBoost);
		return accountBoost;
	}

	private User lockUser(Long userId) {
		return userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private static int valueOrZero(Integer value) {
		return value == null ? 0 : value;
	}

	private static int increment(int value) {
		return value == Integer.MAX_VALUE ? value : value + 1;
	}

	private static double multiplier(int streak, int cap) {
		return 1.0 + BOOST_PER_STREAK * Math.min(streak, cap);
	}

	static double multiplierForNextTask(Integer completedTasks) {
		return multiplier(valueOrZero(completedTasks), TASK_STREAK_BOOST_CAP);
	}

	static double accountMultiplier(User user, LocalDate activityDate) {
		return multiplier(dayStreakForActivity(user, activityDate), DAY_STREAK_BOOST_CAP);
	}

	private static int dayStreakForActivity(User user, LocalDate activityDate) {
		int currentStreak = valueOrZero(user.getDayStreak());
		LocalDate lastActivity = user.getLastActivityDate();
		if (lastActivity == null) {
			return currentStreak > 0 ? currentStreak : 1;
		}
		if (activityDate.equals(lastActivity)) {
			return currentStreak;
		}
		if (activityDate.isBefore(lastActivity)) {
			return currentStreak;
		}
		if (activityDate.isAfter(lastActivity) && lastActivity.plusDays(1).equals(activityDate)) {
			return increment(currentStreak);
		}
		return 1;
	}
}
