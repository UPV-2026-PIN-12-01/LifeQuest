package com.lifequest.service;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.entity.Reward;
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
	public Long create(Long userId, long xp) {
		return create(userId, xp, 0);
	}

	@Transactional
	public Long create(Long userId, long xp, long gold) {
		Reward reward = new Reward();
		reward.setUserId(userId);
		reward.setXpReward(xp);
		reward.setGoldReward(gold);
		return rewardRepository.save(reward).getId();
	}

	@Transactional
	public void updateXp(Long rewardId, long xp) {
		Reward reward = rewardRepository.findById(rewardId)
				.orElseThrow(() -> new ResourceNotFoundException("Reward not found"));
		reward.setXpReward(xp);
	}

	@Transactional(readOnly = true)
	public Reward getById(Long rewardId) {
		return rewardRepository.findById(rewardId)
				.orElseThrow(() -> new ResourceNotFoundException("Reward not found"));
	}

	// XP already granted to the user from from (inclusive) to toExclusive, by claim time.
	@Transactional(readOnly = true)
	public long claimedXpBetween(Long userId, Instant from, Instant toExclusive) {
		return rewardRepository.sumClaimedXp(userId, from, toExclusive);
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
