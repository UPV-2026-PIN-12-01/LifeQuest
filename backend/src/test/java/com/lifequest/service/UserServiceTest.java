package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lifequest.model.entity.User;
import com.lifequest.repository.UserRepository;

class UserServiceTest {

	@Test
	void initializesAccountBoostWhenCreatingUser() {
		UserRepository repository = mock(UserRepository.class);
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserService service = service(repository);

		User created = service.create("test-can-delete-later", "test@example.com", UUID.randomUUID());

		assertThat(created.getDayStreak()).isZero();
		assertThat(created.getXpAccBoost()).isEqualTo(1.0f);
		verify(repository).save(created);
	}

	@Test
	void storesSelectedUserIconWhenCreatingUser() {
		UserRepository repository = mock(UserRepository.class);
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserService service = service(repository);

		User created = service.create(
				"test-can-delete-later", "test@example.com", UUID.randomUUID(), "🪄");

		assertThat(created.getUserIcon()).isEqualTo("🪄");
		verify(repository).save(created);
	}

	@Test
	void synchronizesStoredAccountBoostWhenUpdatingUser() {
		UserRepository repository = mock(UserRepository.class);
		User user = new User();
		user.setDayStreak(99);
		user.setLastActivityDate(LocalDate.of(2026, 11, 5));
		when(repository.findById(7L)).thenReturn(Optional.of(user));
		when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(user));
		when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UserService service = service(repository);

		service.updateProfileUrl(7L, "https://example.com/test-can-delete-later.png");

		assertThat(user.getXpAccBoost()).isEqualTo(1.35f);
		verify(repository).save(user);
	}

	private static UserService service(UserRepository repository) {
		UserStreakService streakService = new UserStreakService(
				repository, Clock.system(ZoneId.of("Europe/Madrid")));
		return new UserService(repository, mock(CharacterStatsService.class), streakService);
	}
}
