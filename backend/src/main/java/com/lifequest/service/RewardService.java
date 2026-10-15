package com.lifequest.service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
