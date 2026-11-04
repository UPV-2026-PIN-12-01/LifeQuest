package com.lifequest.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.InvalidStateException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.GoalDto;
import com.lifequest.model.dto.GoalEvaluationRequestDto;
import com.lifequest.model.dto.GoalRequestDto;
import com.lifequest.model.entity.Goal;
import com.lifequest.model.entity.Reward;
import com.lifequest.repository.GoalRepository;

@Service
public class GoalService {
	private static final String SHOWN = "SHOWN";
	private static final String DELETED = "DELETED";

	private final GoalRepository goalRepository;
	private final UserService userService;
	private final RewardService rewardService;
	private final CharacterStatsService characterStatsService;
	private final Clock clock;

	public GoalService(
			GoalRepository goalRepository,
			UserService userService,
			RewardService rewardService,
			CharacterStatsService characterStatsService,
			Clock clock) {
		this.goalRepository = goalRepository;
		this.userService = userService;
		this.rewardService = rewardService;
		this.characterStatsService = characterStatsService;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public Map<Long, Goal> getGoalsByIds(Collection<Long> goalIds) {
		if (goalIds.isEmpty()) {
			return Map.of();
		}
		return goalRepository.findAllById(goalIds).stream()
				.collect(Collectors.toMap(Goal::getId, goal -> goal));
	}

	@Transactional(readOnly = true)
	public List<GoalDto> findForUser(Long userId) {
		userService.getById(userId);
		return goalRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.filter(goal -> !DELETED.equals(goal.getVisibility()))
				.map(this::toDto)
				.toList();
	}

	@Transactional
	public GoalDto create(Long userId, GoalRequestDto request) {
		userService.getById(userId);
		validateRequest(request);

		long rewardId = rewardService.create(userId, request.xpReward(), request.goldReward());
		Goal goal = new Goal();
		goal.setUserId(userId);
		goal.setName(request.title().trim());
		goal.setDescription(normalize(request.description()));
		goal.setStartDate(atStartOfDay(request.startDate()));
		goal.setEndDate(atStartOfDay(request.endDate()));
		goal.setRewardId(rewardId);
		goal.setCompleted(false);
		goal.setVisibility(SHOWN);
		return toDto(goalRepository.save(goal));
	}

	@Transactional
	public GoalDto evaluate(Long userId, Long goalId, GoalEvaluationRequestDto request) {
		Goal goal = findOwned(userId, goalId);
		if (goal.isCompleted()) {
			throw new InvalidStateException("Goal was already evaluated");
		}
		if (request == null || request.completed() == null) {
			throw new InvalidRequestException("completed is required");
		}

		Short rating = request.rating();
		if (request.completed() && (rating == null || rating < 1 || rating > 5)) {
			throw new InvalidRequestException("rating must be between 1 and 5 for a completed goal");
		}
		goal.setCompleted(true);
		goal.setRating(request.completed() ? rating : null);
		Goal saved = goalRepository.save(goal);

		if (request.completed() && goal.getRewardId() != null) {
			Reward reward = rewardService.getById(goal.getRewardId());
			rewardService.claim(reward, reward.getXpReward(), clock.instant());
			characterStatsService.addXp(userId, reward.getXpReward());
			characterStatsService.addGold(userId, reward.getGoldReward());
		}
		return toDto(saved);
	}

	@Transactional
	public void updateVisibility(Long goalId, Long userId, String visibility) {
		if (!"SHOWN".equals(visibility) && !"HIDDEN".equals(visibility) && !DELETED.equals(visibility)) {
			throw new InvalidRequestException("visibility is invalid");
		}
		Goal goal = findOwned(userId, goalId);
		goal.setVisibility(visibility);
		goalRepository.save(goal);
	}

	private Goal findOwned(Long userId, Long goalId) {
		return goalRepository.findByIdAndUserId(goalId, userId)
				.filter(goal -> !DELETED.equals(goal.getVisibility()))
				.orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
	}

	private void validateRequest(GoalRequestDto request) {
		if (request == null) {
			throw new InvalidRequestException("Goal is required");
		}
		String title = request.title() == null ? "" : request.title().trim();
		if (title.isEmpty() || title.length() > 120) {
			throw new InvalidRequestException("title must be 1 to 120 characters");
		}
		if (request.startDate() == null || request.endDate() == null) {
			throw new InvalidRequestException("startDate and endDate are required");
		}
		if (request.endDate().isBefore(request.startDate())) {
			throw new InvalidRequestException("endDate must not be before startDate");
		}
		if (request.xpReward() < 25 || request.goldReward() < 10) {
			throw new InvalidRequestException("xpReward must be at least 25 and goldReward at least 10");
		}
	}

	private GoalDto toDto(Goal goal) {
		long xpReward = 0;
		long goldReward = 0;
		if (goal.getRewardId() != null) {
			Reward reward = rewardService.getById(goal.getRewardId());
			xpReward = reward.getXpReward();
			goldReward = reward.getGoldReward();
		}
		String status = !goal.isCompleted() ? "pending" : goal.getRating() == null ? "failed" : "completed";
		return new GoalDto(
				goal.getId(),
				goal.getName(),
				goal.getDescription(),
				toDate(goal.getStartDate()),
				toDate(goal.getEndDate()),
				goal.isCompleted(),
				goal.getRating(),
				status,
				xpReward,
				goldReward);
	}

	private static Instant atStartOfDay(LocalDate date) {
		return date.atStartOfDay(ZoneOffset.UTC).toInstant();
	}

	private static LocalDate toDate(Instant instant) {
		return instant == null ? null : instant.atZone(ZoneOffset.UTC).toLocalDate();
	}

	private static String normalize(String value) {
		return value == null || value.isBlank() ? "Sin descripción." : value.trim();
	}
}
