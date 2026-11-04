package com.lifequest.service;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.entity.Reward;
import com.lifequest.model.entity.RewardSource;
import com.lifequest.repository.RewardRepository;

@Service
public class RewardService {

	private final RewardRepository rewardRepository;

	public RewardService(RewardRepository rewardRepository) {
		this.rewardRepository = rewardRepository;
	}

	@Transactional(readOnly = true)
	public Map<Long, Long> xpByRewardId(Collection<Long> rewardIds) {
		if (rewardIds.isEmpty()) {
			return Map.of();
		}
		return rewardRepository.findAllById(rewardIds).stream()
				.collect(Collectors.toMap(Reward::getId, Reward::getXpReward));
	}

	@Transactional
	public Long create(Long userId, long xp, RewardSource source) {
		return create(userId, xp, 0, source);
	}

	@Transactional
	public Long create(Long userId, long xp, long gold, RewardSource source) {
		Reward reward = new Reward();
		reward.setUserId(userId);
		reward.setXpReward(xp);
		reward.setGoldReward(gold);
		reward.setSource(Objects.requireNonNull(source, "Reward source is required"));
		return rewardRepository.save(reward).getId();
	}

	@Transactional
	public void updateXp(Long rewardId, long xp, RewardSource source) {
		Reward reward = rewardRepository.findById(rewardId)
				.orElseThrow(() -> new ResourceNotFoundException("Reward not found"));
		reward.setXpReward(xp);
		reward.setSource(Objects.requireNonNull(source, "Reward source is required"));
	}

	@Transactional(readOnly = true)
	public Reward getById(Long rewardId) {
		return rewardRepository.findById(rewardId)
				.orElseThrow(() -> new ResourceNotFoundException("Reward not found"));
	}

	// XP already granted by task rewards from from (inclusive) to toExclusive, by claim time.
	@Transactional(readOnly = true)
	public long claimedXpForSourceBetween(
			Long userId, RewardSource source, Instant from, Instant toExclusive) {
		return rewardRepository.sumClaimedXpBySource(userId, source, from, toExclusive);
	}

	@Transactional(readOnly = true)
	public long countClaimedBetween(Long userId, Instant from, Instant toExclusive) {
		return rewardRepository.countClaimed(userId, from, toExclusive);
	}

	// The reward keeps the XP actually granted, so the card shows what the user got.
	@Transactional
	public void claim(Reward reward, long grantedXp, Instant now) {
		reward.setXpReward(grantedXp);
		reward.setClaimedAt(now);
		rewardRepository.save(reward);
	}

	@Transactional
	public void delete(Long rewardId) {
		rewardRepository.deleteById(rewardId);
	}
}
