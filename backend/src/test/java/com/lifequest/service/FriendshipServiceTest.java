package com.lifequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.InvalidStateException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.ActivityType;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.Friendship;
import com.lifequest.model.entity.FriendshipStatus;
import com.lifequest.model.entity.PlayerClass;
import com.lifequest.model.entity.User;
import com.lifequest.repository.CharacterStatsRepository;
import com.lifequest.repository.FriendshipRepository;
import com.lifequest.repository.UserRepository;

class FriendshipServiceTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-07T12:00:00Z"), ZoneId.of("Europe/Madrid"));

	private FriendshipRepository friendshipRepository;
	private UserRepository userRepository;
	private CharacterStatsRepository statsRepository;
	private TaskService taskService;
	private FriendshipService service;

	@BeforeEach
	void setUp() {
		friendshipRepository = mock(FriendshipRepository.class);
		userRepository = mock(UserRepository.class);
		statsRepository = mock(CharacterStatsRepository.class);
		taskService = mock(TaskService.class);
		service = new FriendshipService(friendshipRepository, userRepository, statsRepository, taskService, CLOCK);
		when(friendshipRepository.save(any(Friendship.class))).thenAnswer(call -> call.getArgument(0));
	}

	@Test
	void sendRequestCreatesPendingFriendship() {
		when(statsRepository.findByPlayerNameIgnoreCase("Hero-test-can-delete-later")).thenReturn(Optional.of(stats(8L, "Hero-test-can-delete-later")));
		when(friendshipRepository.findBetween(7L, 8L)).thenReturn(Optional.empty());

		service.sendRequest(7L, " Hero-test-can-delete-later ");

		verify(friendshipRepository).save(org.mockito.ArgumentMatchers.argThat(f -> f.getRequesterId() == 7L
				&& f.getAddresseeId() == 8L && f.getStatus() == FriendshipStatus.PENDING));
	}

	@Test
	void sendRequestToSelfIsRejected() {
		when(statsRepository.findByPlayerNameIgnoreCase("Me")).thenReturn(Optional.of(stats(7L, "Me")));

		assertThatThrownBy(() -> service.sendRequest(7L, "Me")).isInstanceOf(InvalidRequestException.class);
	}

	@Test
	void sendRequestToUnknownPlayerIsNotFound() {
		when(statsRepository.findByPlayerNameIgnoreCase("Nobody")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.sendRequest(7L, "Nobody")).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void sendingBackAcceptsTheirPendingRequest() {
		Friendship theirs = friendship(8L, 7L, FriendshipStatus.PENDING);
		when(statsRepository.findByPlayerNameIgnoreCase("Hero")).thenReturn(Optional.of(stats(8L, "Hero")));
		when(friendshipRepository.findBetween(7L, 8L)).thenReturn(Optional.of(theirs));

		service.sendRequest(7L, "Hero");

		assertThat(theirs.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
		assertThat(theirs.getAcceptedAt()).isEqualTo(CLOCK.instant());
	}

	@Test
	void sendingTwiceIsAConflict() {
		when(statsRepository.findByPlayerNameIgnoreCase("Hero")).thenReturn(Optional.of(stats(8L, "Hero")));
		when(friendshipRepository.findBetween(7L, 8L)).thenReturn(Optional.of(friendship(7L, 8L, FriendshipStatus.PENDING)));

		assertThatThrownBy(() -> service.sendRequest(7L, "Hero")).isInstanceOf(InvalidStateException.class);
	}

	@Test
	void onlyTheAddresseeCanAccept() {
		Friendship mine = friendship(7L, 8L, FriendshipStatus.PENDING);
		when(friendshipRepository.findById(1L)).thenReturn(Optional.of(mine));

		assertThatThrownBy(() -> service.acceptRequest(7L, 1L)).isInstanceOf(ResourceNotFoundException.class);
		verify(friendshipRepository, never()).save(mine);
	}

	@Test
	void profileOfNonFriendIsNotFound() {
		when(friendshipRepository.findBetween(7L, 9L)).thenReturn(Optional.of(friendship(7L, 9L, FriendshipStatus.PENDING)));

		assertThatThrownBy(() -> service.profile(7L, 9L)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void profileSharesOnlyCompletedTasksWithoutDescription() {
		when(friendshipRepository.findBetween(7L, 8L)).thenReturn(Optional.of(friendship(7L, 8L, FriendshipStatus.ACCEPTED)));
		User friend = mock(User.class);
		when(friend.getId()).thenReturn(8L);
		when(friend.getStreakDays()).thenReturn(3);
		when(userRepository.findById(8L)).thenReturn(Optional.of(friend));
		when(statsRepository.findByUserId(8L)).thenReturn(Optional.of(stats(8L, "Hero")));
		when(taskService.findForUser(8L, LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-07"))).thenReturn(List.of(
				task(1L, "2026-11-05", "completed"),
				task(2L, "2026-11-06", "pending"),
				task(3L, "2026-11-07", "completed")));

		var profile = service.profile(7L, 8L);

		assertThat(profile.completedTasks()).extracting(t -> t.id()).containsExactly(3L, 1L);
		assertThat(profile.completedTasks()).extracting(t -> t.durationMinutes()).containsOnly(60L);
		assertThat(profile.player().playerName()).isEqualTo("Hero");
		assertThat(profile.dayStreak()).isEqualTo(3);
	}

	@Test
	void feedMixesFriendsTasksNewestFirst() {
		when(friendshipRepository.findByUserAndStatus(7L, FriendshipStatus.ACCEPTED)).thenReturn(List.of(
				friendship(7L, 8L, FriendshipStatus.ACCEPTED), friendship(9L, 7L, FriendshipStatus.ACCEPTED)));
		User ana = mock(User.class);
		when(ana.getId()).thenReturn(8L);
		User luis = mock(User.class);
		when(luis.getId()).thenReturn(9L);
		when(userRepository.findAllById(List.of(8L, 9L))).thenReturn(List.of(ana, luis));
		when(statsRepository.findByUserIdIn(List.of(8L, 9L))).thenReturn(List.of(stats(8L, "Ana"), stats(9L, "Luis")));
		LocalDate from = LocalDate.parse("2026-11-01");
		LocalDate to = LocalDate.parse("2026-11-07");
		when(taskService.findForUser(8L, from, to)).thenReturn(List.of(
				task(1L, "2026-11-06", "completed", "10:00"), task(2L, "2026-11-07", "pending", "12:00")));
		when(taskService.findForUser(9L, from, to)).thenReturn(List.of(
				task(3L, "2026-11-06", "completed", "18:00"), task(4L, "2026-11-07", "completed", "09:00")));

		var feed = service.activityFeed(7L);

		assertThat(feed).extracting(entry -> entry.task().id()).containsExactly(4L, 3L, 1L);
		assertThat(feed).extracting(entry -> entry.task().durationMinutes()).containsExactly(60L, 600L, 120L);
		assertThat(feed).extracting(entry -> entry.player().playerName()).containsExactly("Luis", "Luis", "Ana");
	}

	private static TaskDto task(Long id, String date, String status, String endTime) {
		return new TaskDto(id, "Tarea test-can-delete-later", "Privada", "📘", "purple", date, "08:00", endTime,
				status, "medium", 20, 6, ActivityType.STUDY, "none", null, false, 0);
	}

	private static TaskDto task(Long id, String date, String status) {
		return new TaskDto(id, "Tarea test-can-delete-later", "Privada", "📘", "purple", date, "09:00", "10:00",
				status, "medium", 20, 6, ActivityType.STUDY, "none", null, false, 0);
	}

	private static CharacterStats stats(Long userId, String name) {
		CharacterStats stats = new CharacterStats();
		stats.setUserId(userId);
		stats.setPlayerName(name);
		stats.setPlayerClass(PlayerClass.knight);
		return stats;
	}

	private static Friendship friendship(Long requester, Long addressee, FriendshipStatus status) {
		Friendship friendship = new Friendship();
		friendship.setRequesterId(requester);
		friendship.setAddresseeId(addressee);
		friendship.setStatus(status);
		return friendship;
	}
}
