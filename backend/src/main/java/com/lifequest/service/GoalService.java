package com.lifequest.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.InvalidStateException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.GoalDto;
import com.lifequest.model.dto.GoalEvaluationRequestDto;
import com.lifequest.model.dto.GoalQuotaDto;
import com.lifequest.model.dto.GoalRequestDto;
import com.lifequest.model.entity.Goal;
import com.lifequest.model.entity.GoalRewardTier;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.RewardSource;
import com.lifequest.repository.GoalRepository;

@Service
public class GoalService {
	private static final String SHOWN = "SHOWN";
	private static final String DELETED = "DELETED";
	private static final int WEEKLY_LIMIT = 3;
	private static final int MINIMUM_DURATION_HOURS = 12;
	private static final int LEGACY_REWARD_WAIT_HOURS = 72;

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
	public List<GoalDto> findForUser(Long userId, boolean history) {
		userService.getById(userId);
		return goalRepository.findByUserIdAndCompletedOrderByCreatedAtDesc(userId, history).stream()
				.filter(goal -> !DELETED.equals(goal.getVisibility()))
				.map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public GoalQuotaDto getQuota(Long userId) {
		userService.getById(userId);
		WeekWindow week = currentWeek();
		long created = goalRepository.countByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
				userId, week.start(), week.end());
		return new GoalQuotaDto(created, WEEKLY_LIMIT);
	}

	@Transactional
	public GoalDto create(Long userId, GoalRequestDto request) {
		userService.lockForUpdate(userId);
		GoalRewardTier tier = validateRequest(request);
		WeekWindow week = currentWeek();
		long created = goalRepository.countByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
				userId, week.start(), week.end());
		if (created >= WEEKLY_LIMIT) {
			throw new InvalidStateException("You can create at most three goals per week");
		}

		long rewardId = rewardService.create(userId, tier.xp(), tier.gold(), RewardSource.OBJECTIVE);
		Goal goal = new Goal();
		goal.setUserId(userId);
		goal.setName(request.title().trim());
		goal.setDescription(normalize(request.description()));
		goal.setStartDate(toInstant(request.startDate()));
		goal.setEndDate(toInstant(request.endDate()));
		goal.setRewardId(rewardId);
		goal.setCompleted(false);
		goal.setVisibility(SHOWN);
		return toDto(goalRepository.save(goal));
	}

	@Transactional
	public GoalDto evaluate(Long userId, Long goalId, GoalEvaluationRequestDto request) {
		Goal goal = findOwnedForUpdate(userId, goalId);
		if (goal.isCompleted()) {
			throw new InvalidStateException("Goal was already evaluated");
		}
		if (request == null || request.completed() == null) {
			throw new InvalidRequestException("completed is required");
		}
		if (goal.getEndDate() == null) {
			throw new InvalidStateException("Goal has no end date and cannot be evaluated");
		}
		if (clock.instant().isBefore(goal.getEndDate())) {
			throw new InvalidStateException("Goal cannot be evaluated before its end date");
		}

		Short rating = request.rating();
		if (request.completed() && (rating == null || rating < 1 || rating > 10)) {
			throw new InvalidRequestException("rating must be between 1 and 10");
		}
		goal.setCompleted(true);
		goal.setRating(request.completed() ? rating : null);
		return toDto(goalRepository.save(goal));
	}

	@Transactional
	public GoalDto claim(Long userId, Long goalId) {
		Goal goal = findOwnedForUpdate(userId, goalId);
		if (!goal.isCompleted() || goal.getRating() == null) {
			throw new InvalidStateException("Only successfully completed goals can be claimed");
		}
		if (goal.getRewardId() == null) {
			throw new InvalidStateException("Goal has no reward to claim");
		}

		Reward reward = rewardService.getById(goal.getRewardId());
		if (reward.getClaimedAt() != null) {
			throw new InvalidStateException("Goal reward was already claimed");
		}
		GoalRewardTier tier = GoalRewardTier.findByReward(
				reward.getXpReward(), reward.getGoldReward()).orElse(null);
		Instant claimableAt = claimableAt(goal, tier);
		Instant now = clock.instant();
		if (claimableAt == null) {
			throw new InvalidStateException("Goal has no date from which its reward can become available");
		}
		if (now.isBefore(claimableAt)) {
			throw new InvalidStateException("Goal reward is not available until " + claimableAt);
		}

		long awardedXp = characterStatsService.addXp(userId, reward.getXpReward());
		rewardService.claim(reward, awardedXp, now);
		characterStatsService.addGold(userId, reward.getGoldReward());
		return toDto(goal);
	}

	@Transactional
	public void delete(Long userId, Long goalId) {
		Goal goal = findOwnedForUpdate(userId, goalId);
		goal.setVisibility(DELETED);
		goalRepository.save(goal);
	}

	private GoalRewardTier validateRequest(GoalRequestDto request) {
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
		LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
		if (request.startDate().isBefore(now) || request.endDate().isBefore(now)) {
			throw new InvalidRequestException("Goal dates cannot be in the past");
		}
		if (Duration.between(request.startDate(), request.endDate())
				.compareTo(Duration.ofHours(MINIMUM_DURATION_HOURS)) < 0) {
			throw new InvalidRequestException("A goal must last at least 12 hours");
		}
		return GoalRewardTier.fromLevel(request.rewardTier());
	}

	private Goal findOwnedForUpdate(Long userId, Long goalId) {
		return goalRepository.findForUpdateByIdAndUserId(goalId, userId)
				.filter(goal -> !DELETED.equals(goal.getVisibility()))
				.orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
	}

	private GoalDto toDto(Goal goal) {
		Reward reward = goal.getRewardId() == null ? null : rewardService.getById(goal.getRewardId());
		long xp = reward == null ? 0 : reward.getXpReward();
		long gold = reward == null ? 0 : reward.getGoldReward();
		Optional<GoalRewardTier> tier = reward == null
				? Optional.empty()
				: GoalRewardTier.findByReward(xp, gold);
		String status = !goal.isCompleted() ? "pending" : goal.getRating() == null ? "failed" : "completed";
		return new GoalDto(
				goal.getId(),
				goal.getName(),
				goal.getDescription(),
				goal.getCreatedAt() == null ? clock.instant() : goal.getCreatedAt(),
				toLocalDateTime(goal.getStartDate()),
				toLocalDateTime(goal.getEndDate()),
				goal.isCompleted(),
				goal.getRating(),
				status,
				tier.map(GoalRewardTier::level).orElse(0),
				xp,
				gold,
				reward == null ? null : claimableAt(goal, tier.orElse(null)),
				reward != null && reward.getClaimedAt() != null);
	}

	private Instant claimableAt(Goal goal, GoalRewardTier tier) {
		Instant reference = goal.getEndDate() == null ? goal.getCreatedAt() : goal.getEndDate();
		int waitHours = tier == null ? LEGACY_REWARD_WAIT_HOURS : tier.claimWaitHours();
		return reference == null ? null : reference.plus(Duration.ofHours(waitHours));
	}

	private Instant toInstant(LocalDateTime dateTime) {
		return dateTime.atZone(clock.getZone()).toInstant();
	}

	private LocalDateTime toLocalDateTime(Instant instant) {
		return instant == null ? null : LocalDateTime.ofInstant(instant, clock.getZone());
	}

	private WeekWindow currentWeek() {
		ZoneId zone = clock.getZone();
		LocalDate today = LocalDate.now(clock);
		LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1L);
		return new WeekWindow(
				monday.atStartOfDay(zone).toInstant(),
				monday.plusWeeks(1).atStartOfDay(zone).toInstant());
	}

	private static String normalize(String value) {
		return value == null || value.isBlank() ? "Sin descripción." : value.trim();
	}

	private record WeekWindow(Instant start, Instant end) {
	}
}
