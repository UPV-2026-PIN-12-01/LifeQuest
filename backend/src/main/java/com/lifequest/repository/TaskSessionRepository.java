package com.lifequest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.TaskSession;

public interface TaskSessionRepository extends JpaRepository<TaskSession, Long> {

	Optional<TaskSession> findByTaskId(Long taskId);

	List<TaskSession> findByTaskIdIn(Collection<Long> taskIds);
}
