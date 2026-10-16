package com.lifequest.service;

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
		Reward reward = new Reward();
		reward.setUserId(userId);
		reward.setXpReward(xp);
		reward.setGoldReward(0L);
		return rewardRepository.save(reward).getId();
	}

	@Transactional
	public void updateXp(Long rewardId, long xp) {
		Reward reward = rewardRepository.findById(rewardId)
				.orElseThrow(() -> new ResourceNotFoundException("Reward not found"));
		reward.setXpReward(xp);
	}

	@Transactional
	public void delete(Long rewardId) {
		rewardRepository.deleteById(rewardId);
	}
}
