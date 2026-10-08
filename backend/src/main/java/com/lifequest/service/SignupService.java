package com.lifequest.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.lifequest.exception.DuplicateUserException;
import com.lifequest.exception.InvalidSignupException;
import com.lifequest.model.dto.ProfileDto;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.PlayerClass;
import com.lifequest.model.entity.User;

@Service
public class SignupService {

	private static final Logger log = LoggerFactory.getLogger(SignupService.class);
	private static final int MIN_PASSWORD_LENGTH = 6;
	private static final String EMAIL_SHAPE = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

	private final UserService userService;
	private final CharacterStatsService characterStatsService;
	private final SupabaseAuthService supabaseAuthService;
	private final TransactionTemplate transactionTemplate;

	public SignupService(
			UserService userService,
			CharacterStatsService characterStatsService,
			SupabaseAuthService supabaseAuthService,
			PlatformTransactionManager transactionManager) {
		this.userService = userService;
		this.characterStatsService = characterStatsService;
		this.supabaseAuthService = supabaseAuthService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public ProfileDto signup(String username, String email, String password, String playerClass, String userIcon) {
		String trimmedUsername = requireText(username, "Username is required");
		String normalizedEmail = requireText(email, "Email is required").toLowerCase();
		if (!normalizedEmail.matches(EMAIL_SHAPE)) {
			throw new InvalidSignupException("Invalid email");
		}
		if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
			throw new InvalidSignupException("Password must be at least 6 characters");
		}

		PlayerClass resolvedClass = resolveClass(playerClass);
		String icon = StringUtils.hasText(userIcon) ? userIcon.trim() : resolvedClass.defaultIcon();

		if (userService.existsByUsername(trimmedUsername) || userService.existsByEmail(normalizedEmail)) {
			throw new DuplicateUserException("Username or email already exists");
		}

		UUID authId = supabaseAuthService.createUser(normalizedEmail, password);
		try {
			transactionTemplate.executeWithoutResult(status -> {
				User user = userService.create(trimmedUsername, normalizedEmail, authId, icon);
				CharacterStats stats = characterStatsService.createForUser(user.getId(), trimmedUsername, resolvedClass);
				userService.linkCharacter(user, stats);
			});
		} catch (RuntimeException ex) {
			rollbackAuth(authId);
			if (ex instanceof DataIntegrityViolationException) {
				throw new DuplicateUserException("Username or email already exists");
			}
			throw ex;
		}

		return new ProfileDto(trimmedUsername);
	}

	private static String requireText(String value, String message) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidSignupException(message);
		}
		return value.trim();
	}

	private static PlayerClass resolveClass(String playerClass) {
		try {
			return PlayerClass.fromInput(playerClass);
		} catch (IllegalArgumentException ex) {
			throw new InvalidSignupException(ex.getMessage());
		}
	}

	private void rollbackAuth(UUID authId) {
		try {
			supabaseAuthService.deleteUser(authId);
		} catch (RuntimeException ex) {
			log.warn("Could not delete Auth user {} after profile insert failed", authId, ex);
		}
	}
}
