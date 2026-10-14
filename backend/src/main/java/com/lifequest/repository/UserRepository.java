package com.lifequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifequest.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
