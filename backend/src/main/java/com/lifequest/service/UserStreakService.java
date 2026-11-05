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
		LocalDate lastActivity = user.getLastActivityDate();
		int dayStreak = valueOrZero(user.getDayStreak());

		if (lastActivity == null) {
			dayStreak = 1;
			user.setLastActivityDate(today);
		} else if (today.isAfter(lastActivity)) {
			dayStreak = lastActivity.plusDays(1).equals(today) ? increment(dayStreak) : 1;
			user.setLastActivityDate(today);
		}

		user.setDayStreak(dayStreak);
		float accountBoost = (float) multiplier(dayStreak, DAY_STREAK_BOOST_CAP);
		user.setXpAccBoost(accountBoost);
		userRepository.save(user);
		return accountBoost;
	}

	@Transactional
	public double recordTaskCompletion(Long userId) {
		User user = lockUser(userId);
		int taskStreak = increment(valueOrZero(user.getTaskStreak()));
		user.setTaskStreak(taskStreak);
		userRepository.save(user);
		return multiplier(taskStreak, TASK_STREAK_BOOST_CAP);
	}

	@Transactional
	public void recordTaskMiss(Long userId) {
		User user = lockUser(userId);
		user.setTaskStreak(0);
		userRepository.save(user);
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
}
