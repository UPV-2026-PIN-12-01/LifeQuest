package com.lifequest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.CharacterStats;

public interface CharacterStatsRepository extends JpaRepository<CharacterStats, Long> {

	Optional<CharacterStats> findByUserId(Long userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select stats from CharacterStats stats where stats.userId = :userId")
	Optional<CharacterStats> findByUserIdForUpdate(@Param("userId") Long userId);

	Optional<CharacterStats> findByPlayerNameIgnoreCase(String playerName);

	// Players whose name contains the (already LIKE-escaped) text, those starting with it first, then shortest.
	@Query("""
			select stats from CharacterStats stats
			where stats.userId is not null and stats.userId <> :excludedUserId
			and lower(stats.playerName) like concat('%', :text, '%') escape '\\'
			order by case when lower(stats.playerName) like concat(:text, '%') escape '\\' then 0 else 1 end,
			length(stats.playerName), lower(stats.playerName)
			""")
	List<CharacterStats> searchByPlayerName(@Param("text") String lowerCaseEscapedText,
			@Param("excludedUserId") Long excludedUserId, Pageable page);

	List<CharacterStats> findByUserIdIn(Collection<Long> userIds);

	boolean existsByPlayerName(String playerName);

	boolean existsByPlayerNameAndUserIdNot(String playerName, Long userId);
}
