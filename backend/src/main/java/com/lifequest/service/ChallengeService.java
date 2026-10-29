package com.lifequest.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.InvalidStateException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.ChallengeDto;
import com.lifequest.model.dto.ChallengeEvaluationRequestDto;
import com.lifequest.model.dto.ChallengeRequestDto;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.entity.Challenge;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.Task;
import com.lifequest.repository.ChallengeRepository;

@Service
public class ChallengeService {

	static final String VISIBLE = "SHOWN";
	static final String DELETED = "DELETED";
	static final Set<String> DIFFICULTIES = Set.of("EASY", "NORMAL", "HARD", "VERY HARD", "LEGENDARY FEAT");
	private static final Map<String, long[]> REWARDS = Map.of(
			"EASY", new long[] { 25, 10 },
			"NORMAL", new long[] { 40, 15 },
			"HARD", new long[] { 75, 25 },
			"VERY HARD", new long[] { 120, 40 },
			"LEGENDARY FEAT", new long[] { 200, 75 });
	private static final long DAILY_CLAIM_LIMIT = 3;

	private final ChallengeRepository challengeRepository;
	private final TaskService taskService;
	private final RewardService rewardService;
	private final CharacterStatsService characterStatsService;
	private final UserService userService;
	private final Clock clock;

	public ChallengeService(ChallengeRepository challengeRepository, TaskService taskService,
			RewardService rewardService, CharacterStatsService characterStatsService,
			UserService userService, Clock clock) {
		this.challengeRepository = challengeRepository;
		this.taskService = taskService;
		this.rewardService = rewardService;
		this.characterStatsService = characterStatsService;
		this.userService = userService;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<ChallengeDto> findForUser(Long userId, boolean history) {
		userService.getById(userId);
		return challengeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.filter(challenge -> !DELETED.equals(challenge.getVisibility()))
				.filter(challenge -> history == challenge.isCompleted())
				.map(this::toDto)
				.toList();
	}

	@Transactional
	public ChallengeDto create(Long userId, ChallengeRequestDto request) {
		userService.getById(userId);
		validateRequest(request);
		Task task = taskService.findOwned(userId, request.taskId());
		String difficulty = request.difficulty().trim().toUpperCase();
		long[] reward = REWARDS.get(difficulty);

		Challenge challenge = new Challenge();
		challenge.setUserId(userId);
		challenge.setTaskId(task.getId());
		challenge.setName(request.name().trim());
		challenge.setDescription(normalize(request.description()));
		challenge.setRequirement(normalize(request.requirement()));
		challenge.setDifficulty(difficulty);
		challenge.setCompleted(false);
		challenge.setVisibility(VISIBLE);
		challenge.setRewardId(rewardService.create(userId, reward[0], reward[1]));
		return toDto(challengeRepository.save(challenge));
	}

	@Transactional
	public ChallengeDto evaluate(Long userId, Long challengeId, ChallengeEvaluationRequestDto request) {
		Challenge challenge = findOwned(userId, challengeId);
		if (challenge.isCompleted()) {
			throw new InvalidStateException("Challenge was already evaluated successfully");
		}
		if (request == null || request.completed() == null) {
			throw new InvalidRequestException("completed is required");
		}
		if (!request.completed()) {
			challenge.setCompleted(true);
			challenge.setRating(null);
			return toDto(challengeRepository.save(challenge));
		}
		if (request.rating() == null || request.rating() < 1 || request.rating() > 10) {
			throw new InvalidRequestException("rating must be between 1 and 10");
		}

		challenge.setCompleted(true);
		challenge.setRating(request.rating());
		return toDto(challengeRepository.save(challenge));
	}

	@Transactional
	public ChallengeDto claim(Long userId, Long challengeId) {
		Challenge challenge = findOwned(userId, challengeId);
		if (!challenge.isCompleted()) {
			throw new InvalidStateException("Challenge must be completed before claiming its reward");
		}
		if (challenge.getRating() == null) {
			throw new InvalidStateException("Only successfully completed challenges can be claimed");
		}
		if (challenge.getTaskId() != null) {
			Task task = taskService.findOwned(userId, challenge.getTaskId());
			if (!TaskService.DB_STATUS_DONE.equals(task.getStatus())) {
				throw new InvalidStateException("Complete the linked task before claiming this reward");
			}
		}
		Instant now = clock.instant();
		ZoneId zone = clock.getZone();
		Instant startOfDay = LocalDate.now(clock).atStartOfDay(zone).toInstant();
		Instant startOfNextDay = LocalDate.now(clock).plusDays(1).atStartOfDay(zone).toInstant();
		if (rewardService.countClaimedBetween(userId, startOfDay, startOfNextDay) >= DAILY_CLAIM_LIMIT) {
			throw new InvalidStateException("You can claim only three challenges per day");
		}
		if (challenge.getRewardId() != null) {
			Reward reward = rewardService.getById(challenge.getRewardId());
			if (reward.getClaimedAt() != null) {
				throw new InvalidStateException("Challenge reward was already claimed");
			}
			rewardService.claim(reward, reward.getXpReward(), now);
			characterStatsService.addXp(userId, reward.getXpReward());
		}
		return toDto(challenge);
	}

	@Transactional
	public void delete(Long userId, Long challengeId) {
		Challenge challenge = findOwned(userId, challengeId);
		challenge.setVisibility(DELETED);
		challengeRepository.save(challenge);
	}

	Challenge findOwned(Long userId, Long challengeId) {
		return challengeRepository.findByIdAndUserId(challengeId, userId)
				.filter(challenge -> !DELETED.equals(challenge.getVisibility()))
				.orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));
	}

	private void validateRequest(ChallengeRequestDto request) {
		if (request == null) {
			throw new InvalidRequestException("Challenge is required");
		}
		String name = request.name() == null ? "" : request.name().trim();
		if (name.isEmpty() || name.length() > 120) {
			throw new InvalidRequestException("name must be 1 to 120 characters");
		}
		if (request.taskId() == null) {
			throw new InvalidRequestException("taskId is required");
		}
		if (request.difficulty() == null || !DIFFICULTIES.contains(request.difficulty().trim().toUpperCase())) {
			throw new InvalidRequestException("difficulty is invalid");
		}
	}

	private ChallengeDto toDto(Challenge challenge) {
		TaskDto task = null;
		if (challenge.getTaskId() != null) {
			task = taskService.describe(taskService.findOwned(challenge.getUserId(), challenge.getTaskId()));
		}
		long xp = 0;
		long gold = 0;
		boolean rewardClaimed = false;
		if (challenge.getRewardId() != null) {
			Reward reward = rewardService.getById(challenge.getRewardId());
			xp = reward.getXpReward();
			gold = reward.getGoldReward();
			rewardClaimed = reward.getClaimedAt() != null;
		}
		return new ChallengeDto(challenge.getId(), challenge.getCreatedAt(), challenge.getName(),
				challenge.getDescription(), challenge.getDifficulty(), challenge.isCompleted(),
				challenge.getTaskId(), challenge.getRating(), challenge.getVisibility(),
				challenge.getRequirement(), xp, gold, rewardClaimed, task);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim();
	}
}
