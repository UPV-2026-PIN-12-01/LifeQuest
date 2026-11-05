package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lifequest.model.dto.UserAccountResponse;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.PlayerClass;
import com.lifequest.model.entity.User;

class UserProfileServiceTest {

	@Test
	void currentUserResponseIncludesStreakFromUser() {
		UserService userService = mock(UserService.class);
		CharacterStatsService statsService = mock(CharacterStatsService.class);
		UserStreakService streakService = mock(UserStreakService.class);
		SupabaseAuthService authService = mock(SupabaseAuthService.class);
		SupabaseStorageService storageService = mock(SupabaseStorageService.class);
		User user = mock(User.class);
		CharacterStats stats = mock(CharacterStats.class);
		when(userService.getByAuthSubject("auth-subject")).thenReturn(user);
		when(user.getId()).thenReturn(7L);
		when(user.getUsername()).thenReturn("test-can-delete-later");
		when(user.getEmail()).thenReturn("test@example.com");
		when(user.getUserIcon()).thenReturn("⚔️");
		when(user.getProfileUrl()).thenReturn("");
		when(user.getStreakDays()).thenReturn(4);
		when(user.getTaskStreak()).thenReturn(3);
		when(streakService.synchronizeAccountBoost(7L)).thenReturn(1.2);
		when(statsService.getByUserId(7L)).thenReturn(stats);
		when(stats.getPlayerName()).thenReturn("Hero");
		when(stats.getPlayerClass()).thenReturn(PlayerClass.archer);

		UserAccountResponse response = new UserProfileService(
				userService, statsService, streakService, authService, storageService).currentUser("auth-subject");

		assertThat(response.user().streakDays()).isEqualTo(4);
		assertThat(response.user().dayStreak()).isEqualTo(4);
		assertThat(response.user().taskStreak()).isEqualTo(3);
		assertThat(response.user().xpAccBoost()).isEqualTo(1.2f);
	}
}
