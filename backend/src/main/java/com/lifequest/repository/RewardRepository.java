package com.lifequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.Reward;

public interface RewardRepository extends JpaRepository<Reward, Long> {
}
