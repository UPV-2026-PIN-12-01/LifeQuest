package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lifequest.model.entity.User;
import com.lifequest.repository.UserRepository;

class UserStreakServiceTest {
	private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");
	private static final LocalDate TODAY = LocalDate.of(2026, 10, 10);

	@Test
	void firstXpActivityStartsTheDailyStreakAndPersistsItsBoost() {
		UserRepository repository = mock(UserRepository.class);
		User user = new User();
		when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		double boost = service(repository, TODAY).recordXpActivity(7L);

		assertThat(boost).isCloseTo(1.05, org.assertj.core.api.Assertions.within(0.000001));
		assertThat(user.getDayStreak()).isEqualTo(1);
		assertThat(user.getLastActivityDate()).isEqualTo(TODAY);
		assertThat(user.getXpAccBoost()).isEqualTo(1.05f);
	}

	@Test
	void activityOnConsecutiveCalendarDaysIncrementsAndCapsTheDailyStreak() {
		UserRepository repository = mock(UserRepository.class);
		User user = user(6, TODAY.minusDays(1), 1.3f);
		when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		double boost = service(repository, TODAY).recordXpActivity(7L);

		assertThat(boost).isCloseTo(1.35, org.assertj.core.api.Assertions.within(0.000001));
		assertThat(user.getDayStreak()).isEqualTo(7);
		assertThat(user.getLastActivityDate()).isEqualTo(TODAY);
	}

	@Test
	void missedCalendarDayRestartsTheDailyStreakAndSameDayXpDoesNotIncrementIt() {
		UserRepository repository = mock(UserRepository.class);
		User user = user(5, TODAY.minusDays(2), 1.25f);
		when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserStreakService service = service(repository, TODAY);

		assertThat(service.recordXpActivity(7L))
				.isCloseTo(1.05, org.assertj.core.api.Assertions.within(0.000001));
		assertThat(user.getDayStreak()).isEqualTo(1);
		assertThat(service(repository, TODAY).recordXpActivity(7L))
				.isCloseTo(1.05, org.assertj.core.api.Assertions.within(0.000001));
		assertThat(user.getDayStreak()).isEqualTo(1);
	}

	@Test
	void taskStreakIncrementsToItsCapAndMissResetsIt() {
		UserRepository repository = mock(UserRepository.class);
		User user = user(9, null, 1.0f);
		user.setTaskStreak(9);
		when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserStreakService service = service(repository, TODAY);

		assertThat(service.recordTaskCompletion(7L)).isEqualTo(1.45);
		assertThat(user.getTaskStreak()).isEqualTo(10);
		assertThat(service.recordTaskCompletion(7L)).isEqualTo(1.5);
		assertThat(user.getTaskStreak()).isEqualTo(11);

		service.recordTaskMiss(7L);

		assertThat(user.getTaskStreak()).isZero();
		verify(repository, org.mockito.Mockito.atLeast(3)).save(user);
	}

	@Test
	void taskStreakFiveGrantsTwentyFivePercentAndThenAdvances() {
		UserRepository repository = mock(UserRepository.class);
		User user = user(0, null, 1.0f);
		user.setTaskStreak(5);
		when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThat(service(repository, TODAY).recordTaskCompletion(7L)).isEqualTo(1.25);
		assertThat(user.getTaskStreak()).isEqualTo(6);
	}

	@Test
	void currentAccountBoostUsesTheDayStreakEvenWhenNoActivityDateHasBeenRecordedYet() {
		UserRepository repository = mock(UserRepository.class);
		User user = user(99, null, 1.0f);
		when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThat(service(repository, TODAY).synchronizeAccountBoost(7L)).isEqualTo(1.35);
		assertThat(user.getXpAccBoost()).isEqualTo(1.35f);
		verify(repository).save(user);
	}

	private static UserStreakService service(UserRepository repository, LocalDate today) {
		Instant instant = today.atTime(12, 0).atZone(MADRID).toInstant();
		return new UserStreakService(repository, Clock.fixed(instant, MADRID));
	}

	private static User user(int dayStreak, LocalDate lastActivityDate, float boost) {
		User user = new User();
		user.setDayStreak(dayStreak);
		user.setLastActivityDate(lastActivityDate);
		user.setXpAccBoost(boost);
		return user;
	}
}
