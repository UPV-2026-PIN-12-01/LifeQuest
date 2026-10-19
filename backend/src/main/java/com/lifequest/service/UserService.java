package com.lifequest.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifequest.exception.DuplicateUserException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.User;
import com.lifequest.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public User getById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	// The Supabase JWT subject is the auth user UUID stored in users.inner_id.
	@Transactional(readOnly = true)
	public User getByAuthSubject(String subject) {
		UUID innerId;
		try {
			innerId = UUID.fromString(subject);
		} catch (IllegalArgumentException | NullPointerException ex) {
			throw new ResourceNotFoundException("User not found");
		}
		return userRepository.findByInnerId(innerId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	@Transactional(readOnly = true)
	public Long getIdByAuthSubject(String subject) {
		return getByAuthSubject(subject).getId();
	}

	// Null username or userIcon means leave that field as it is.
	@Transactional
	public User updateAccount(String subject, String username, String userIcon) {
		User user = getByAuthSubject(subject);
		if (username == null && userIcon == null) {
			return user;
		}
		if (username != null) {
			if (userRepository.existsByUsernameAndIdNot(username, user.getId())) {
				throw new DuplicateUserException("Username already exists");
			}
			user.setUsername(username);
		}
		if (userIcon != null) {
			user.setUserIcon(userIcon);
		}
		try {
			return userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException ex) {
			throw new DuplicateUserException("Username already exists");
		}
	}

	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional
	public User create(String username, String email, UUID innerId, String userIcon) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setInnerId(innerId);
		user.setUserIcon(userIcon);
		return userRepository.save(user);
	}

	@Transactional
	public User linkCharacter(User user, CharacterStats characterStats) {
		user.setPlayerName(characterStats.getPlayerName());
		user.setPlayerId(characterStats.getId());
		return userRepository.save(user);
	}

	@Transactional
	public void updateProfileUrl(Long userId, String profileUrl) {
		User user = userRepository.findById(userId).orElseThrow();
		user.setProfileUrl(profileUrl);
		userRepository.save(user);
	}
}
