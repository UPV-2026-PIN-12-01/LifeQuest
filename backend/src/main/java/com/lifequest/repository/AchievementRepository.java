package com.lifequest.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lifequest.model.entity.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
	List<Achievement> findByUserId(Long userId);
	Optional<Achievement> findByUserIdAndIndexId(Long userId, Long indexId);
}
