package com.lifequest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.TaskSession;

public interface TaskSessionRepository extends JpaRepository<TaskSession, Long> {

	Optional<TaskSession> findByTaskId(Long taskId);

	List<TaskSession> findByTaskIdIn(Collection<Long> taskIds);

	@Query("select count(session) from TaskSession session where session.userId = :userId and session.focusedSeconds > 0 and session.endedAt is not null")
	long countFocusedByUserId(@Param("userId") Long userId);
}
