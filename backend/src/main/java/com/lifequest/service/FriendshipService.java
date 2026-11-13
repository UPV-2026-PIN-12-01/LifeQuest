package com.lifequest.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.InvalidStateException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.FriendActivityDto;
import com.lifequest.model.dto.FriendProfileDto;
import com.lifequest.model.dto.FriendRequestDto;
import com.lifequest.model.dto.FriendSummaryDto;
import com.lifequest.model.dto.FriendTaskDto;
import com.lifequest.model.dto.FriendsOverviewDto;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.Friendship;
import com.lifequest.model.entity.FriendshipStatus;
import com.lifequest.model.entity.User;
import com.lifequest.repository.CharacterStatsRepository;
import com.lifequest.repository.FriendshipRepository;
import com.lifequest.repository.UserRepository;

// Friend requests by playerName. A friend sees your character and the tasks you completed in the last week.
// Anything about someone who is not an accepted friend answers 404, so the API does not reveal who exists.
@Service
public class FriendshipService {

	static final int COMPLETED_TASK_DAYS = 7;
	static final String COMPLETED = "completed";
	static final int FEED_LIMIT = 100;

	private final FriendshipRepository friendshipRepository;
	private final UserRepository userRepository;
	private final CharacterStatsRepository characterStatsRepository;
	private final TaskService taskService;
	private final Clock clock;

	public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository,
			CharacterStatsRepository characterStatsRepository, TaskService taskService, Clock clock) {
		this.friendshipRepository = friendshipRepository;
		this.userRepository = userRepository;
		this.characterStatsRepository = characterStatsRepository;
		this.taskService = taskService;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public FriendsOverviewDto overview(Long userId) {
		List<Friendship> accepted = friendshipRepository.findByUserAndStatus(userId, FriendshipStatus.ACCEPTED);
		List<Friendship> incoming = friendshipRepository
				.findByAddresseeIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.PENDING);
		List<Friendship> outgoing = friendshipRepository
				.findByRequesterIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.PENDING);

		List<Long> otherIds = Stream.of(accepted, incoming, outgoing)
				.flatMap(List::stream)
				.map(friendship -> friendship.otherUserId(userId))
				.distinct()
				.toList();
		Map<Long, FriendSummaryDto> summaries = summaries(otherIds);

		List<FriendSummaryDto> friends = accepted.stream()
				.map(friendship -> summaries.get(friendship.otherUserId(userId)))
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(FriendSummaryDto::playerName, String.CASE_INSENSITIVE_ORDER))
				.toList();
		return new FriendsOverviewDto(friends, requests(incoming, userId, summaries, true),
				requests(outgoing, userId, summaries, false));
	}

	// If the other player had already asked you, sending a request back accepts theirs.
	@Transactional
	public FriendRequestDto sendRequest(Long userId, String playerName) {
		CharacterStats target = characterStatsRepository.findByPlayerNameIgnoreCase(playerName.trim())
				.filter(stats -> stats.getUserId() != null)
				.orElseThrow(() -> new ResourceNotFoundException("Player not found"));
		Long targetId = target.getUserId();
		if (targetId.equals(userId)) {
			throw new InvalidRequestException("You cannot add yourself as a friend");
		}

		Friendship friendship = friendshipRepository.findBetween(userId, targetId).orElse(null);
		if (friendship == null) {
			friendship = new Friendship();
			friendship.setRequesterId(userId);
			friendship.setAddresseeId(targetId);
			friendship.setStatus(FriendshipStatus.PENDING);
		} else if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
			throw new InvalidStateException("You are already friends");
		} else if (friendship.getRequesterId().equals(userId)) {
			throw new InvalidStateException("Friend request already sent");
		} else {
			accept(friendship);
		}
		Friendship saved = friendshipRepository.save(friendship);
		return toRequest(saved, userId, summaries(List.of(targetId)), false);
	}

	@Transactional
	public void acceptRequest(Long userId, Long requestId) {
		Friendship friendship = friendshipRepository.findById(requestId)
				.filter(f -> f.getStatus() == FriendshipStatus.PENDING && userId.equals(f.getAddresseeId()))
				.orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
		accept(friendship);
		friendshipRepository.save(friendship);
	}

	// Rejects an incoming request or cancels one you sent.
	@Transactional
	public void deleteRequest(Long userId, Long requestId) {
		Friendship friendship = friendshipRepository.findById(requestId)
				.filter(f -> f.getStatus() == FriendshipStatus.PENDING && f.involves(userId))
				.orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
		friendshipRepository.delete(friendship);
	}

	@Transactional
	public void removeFriend(Long userId, Long friendUserId) {
		friendshipRepository.delete(acceptedFriendship(userId, friendUserId));
	}

	@Transactional(readOnly = true)
	public FriendProfileDto profile(Long userId, Long friendUserId) {
		acceptedFriendship(userId, friendUserId);
		User friend = userRepository.findById(friendUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Friend not found"));
		CharacterStats stats = characterStatsRepository.findByUserId(friendUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Friend not found"));
		int level = level(stats);

		List<FriendTaskDto> completed = recentCompletedTasks(friendUserId).stream()
				.map(FriendshipService::toFriendTask)
				.toList();

		return new FriendProfileDto(
				summary(friend, stats),
				orZero(stats.getHp()),
				orZero(stats.getStrength()),
				orZero(stats.getSpeed()),
				orZero(stats.getStamina()),
				orZero(stats.getWisdom()),
				orZero(stats.getXpCurrent()),
				CharacterStatsService.xpForNextLevel(level),
				friend.getStreakDays(),
				friend.getTaskStreak() == null ? 0 : friend.getTaskStreak(),
				completed);
	}

	// Completed tasks of all friends in the last week, newest first. Sorted by end time on the server so the
	// order is right within a day, but the times themselves are not sent.
	@Transactional(readOnly = true)
	public List<FriendActivityDto> activityFeed(Long userId) {
		List<Long> friendIds = friendshipRepository.findByUserAndStatus(userId, FriendshipStatus.ACCEPTED).stream()
				.map(friendship -> friendship.otherUserId(userId))
				.distinct()
				.toList();
		Map<Long, FriendSummaryDto> summaries = summaries(friendIds);
		record Entry(FriendSummaryDto player, TaskDto task) {
		}
		return friendIds.stream()
				.filter(summaries::containsKey)
				.flatMap(friendId -> recentCompletedTasks(friendId).stream()
						.map(task -> new Entry(summaries.get(friendId), task)))
				.sorted(Comparator.comparing((Entry entry) -> entry.task().date() + " " + entry.task().endTime())
						.reversed())
				.limit(FEED_LIMIT)
				.map(entry -> new FriendActivityDto(entry.player(), toFriendTask(entry.task())))
				.toList();
	}

	// Completed tasks of the last COMPLETED_TASK_DAYS days, newest first.
	private List<TaskDto> recentCompletedTasks(Long friendUserId) {
		LocalDate today = LocalDate.now(clock);
		return taskService.findForUser(friendUserId, today.minusDays(COMPLETED_TASK_DAYS - 1L), today).stream()
				.filter(task -> COMPLETED.equals(task.status()))
				.sorted(Comparator.comparing((TaskDto task) -> task.date() + " " + task.endTime()).reversed())
				.toList();
	}

	private static FriendTaskDto toFriendTask(TaskDto task) {
		return new FriendTaskDto(task.id(), task.title(), task.icon(), task.activityType(), task.date(),
				durationMinutes(task), task.xp());
	}

	// Times are HH:mm on the same day (day_task_check), so the planned length is their difference.
	static long durationMinutes(TaskDto task) {
		return Duration.between(LocalTime.parse(task.startTime()), LocalTime.parse(task.endTime())).toMinutes();
	}

	private Friendship acceptedFriendship(Long userId, Long friendUserId) {
		return friendshipRepository.findBetween(userId, friendUserId)
				.filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
				.orElseThrow(() -> new ResourceNotFoundException("Friend not found"));
	}

	private void accept(Friendship friendship) {
		friendship.setStatus(FriendshipStatus.ACCEPTED);
		friendship.setAcceptedAt(clock.instant());
	}

	private Map<Long, FriendSummaryDto> summaries(List<Long> userIds) {
		if (userIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, CharacterStats> statsByUser = characterStatsRepository.findByUserIdIn(userIds).stream()
				.collect(Collectors.toMap(CharacterStats::getUserId, Function.identity(), (a, b) -> a));
		return userRepository.findAllById(userIds).stream()
				.filter(user -> statsByUser.containsKey(user.getId()))
				.collect(Collectors.toMap(User::getId, user -> summary(user, statsByUser.get(user.getId()))));
	}

	private List<FriendRequestDto> requests(List<Friendship> friendships, Long userId,
			Map<Long, FriendSummaryDto> summaries, boolean incoming) {
		return friendships.stream()
				.filter(friendship -> summaries.containsKey(friendship.otherUserId(userId)))
				.map(friendship -> toRequest(friendship, userId, summaries, incoming))
				.toList();
	}

	private static FriendRequestDto toRequest(Friendship friendship, Long userId,
			Map<Long, FriendSummaryDto> summaries, boolean incoming) {
		return new FriendRequestDto(friendship.getId(), summaries.get(friendship.otherUserId(userId)), incoming,
				friendship.getCreatedAt() == null ? null : friendship.getCreatedAt().toString());
	}

	private static FriendSummaryDto summary(User user, CharacterStats stats) {
		return new FriendSummaryDto(user.getId(), stats.getPlayerName(), stats.getPlayerClass().displayName(),
				user.getUserIcon(), user.getProfileUrl(), level(stats));
	}

	private static int level(CharacterStats stats) {
		return stats.getLevel() == null ? 1 : stats.getLevel();
	}

	private static long orZero(Long value) {
		return value == null ? 0 : value;
	}

	private static int orZero(Integer value) {
		return value == null ? 0 : value;
	}
}
