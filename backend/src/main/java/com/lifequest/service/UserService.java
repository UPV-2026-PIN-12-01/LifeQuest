package com.lifequest.service;

import java.util.Locale;
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

	@Transactional(readOnly = true)
	public void ensureUsernameAvailable(String username, Long userId) {
		if (userRepository.existsByUsernameAndIdNot(username, userId)) {
			throw new DuplicateUserException("Username already exists");
		}
	}

	@Transactional(readOnly = true)
	public void ensureEmailAvailable(String email, Long userId) {
		if (userRepository.existsByEmailAndIdNot(email, userId)) {
			throw new DuplicateUserException("Email already exists");
		}
	}

	// Null username, userIcon, or email means leave that field as it is.
	@Transactional
	public User updateAccount(String subject, String username, String userIcon, String email) {
		User user = getByAuthSubject(subject);
		if (username == null && userIcon == null && email == null) {
			return user;
		}
		if (username != null) {
			ensureUsernameAvailable(username, user.getId());
			user.setUsername(username);
		}
		if (email != null) {
			ensureEmailAvailable(email, user.getId());
			user.setEmail(email);
		}
		if (userIcon != null) {
			user.setUserIcon(userIcon);
		}
		try {
			return userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException ex) {
			throw duplicateFromIntegrity(ex);
		}
	}

	private static DuplicateUserException duplicateFromIntegrity(DataIntegrityViolationException ex) {
		String text = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase(Locale.ROOT);
		if (text.contains("users_email_key") || text.contains("(email)")) {
			return new DuplicateUserException("Email already exists");
		}
		return new DuplicateUserException("Username already exists");
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
