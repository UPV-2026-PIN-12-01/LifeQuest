package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.lifequest.model.dto.PlayerProfileDto;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.PlayerClass;
import com.lifequest.model.entity.User;

class PlayerServiceTest {

	@Test
	void playerProfileUsesStreakFromUser() {
		UserService userService = mock(UserService.class);
		CharacterStatsService statsService = mock(CharacterStatsService.class);
		TaskService taskService = mock(TaskService.class);
		UserStreakService streakService = mock(UserStreakService.class);
		User user = mock(User.class);
		CharacterStats stats = mock(CharacterStats.class);
		when(userService.getById(7L)).thenReturn(user);
		when(user.getId()).thenReturn(7L);
		when(user.getUsername()).thenReturn("test-can-delete-later");
		when(user.getUserIcon()).thenReturn("🪄");
		when(user.getStreakDays()).thenReturn(6);
		when(user.getTaskStreak()).thenReturn(4);
		when(streakService.synchronizeAccountBoost(7L)).thenReturn(1.3);
		when(statsService.getByUserId(7L)).thenReturn(stats);
		when(stats.getLevel()).thenReturn(2);
		when(stats.getXpCurrent()).thenReturn(4L);
		when(stats.getGold()).thenReturn(75L);
		when(stats.getPlayerName()).thenReturn("Hero");
		when(stats.getPlayerClass()).thenReturn(PlayerClass.knight);

		PlayerProfileDto profile = new PlayerService(userService, statsService, taskService, streakService).getProfile(7L);

		assertThat(profile.streakDays()).isEqualTo(6);
		assertThat(profile.dayStreak()).isEqualTo(6);
		assertThat(profile.taskStreak()).isEqualTo(4);
		assertThat(profile.xpAccBoost()).isEqualTo(1.3f);
		assertThat(profile.gold()).isEqualTo(75L);
		assertThat(profile.userIcon()).isEqualTo("🪄");
	}

	@Test
	void dailyStreakQuestProgressUsesUserStreak() {
		UserService userService = mock(UserService.class);
		CharacterStatsService statsService = mock(CharacterStatsService.class);
		TaskService taskService = mock(TaskService.class);
		UserStreakService streakService = mock(UserStreakService.class);
		User user = mock(User.class);
		when(userService.getById(7L)).thenReturn(user);
		when(user.getStreakDays()).thenReturn(3);
		when(taskService.countCompletedToday(7L)).thenReturn(1L);

		var quests = new PlayerService(userService, statsService, taskService, streakService).getDailyQuests(7L);

		assertThat(quests).filteredOn(quest -> quest.id().equals("streak"))
				.singleElement()
				.satisfies(quest -> assertThat(quest.progress()).isEqualTo(3));
	}
}
