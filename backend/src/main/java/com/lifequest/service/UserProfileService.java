package com.lifequest.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.model.dto.UpdateUserRequest;
import com.lifequest.model.dto.UserAccountDto;
import com.lifequest.model.dto.UserAccountResponse;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.entity.User;

@Service
public class UserProfileService {

	private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);
	// Keep MIN_PASSWORD_LENGTH and EMAIL_SHAPE in sync with SignupService.java
	private static final int MIN_PASSWORD_LENGTH = 6;
	private static final String EMAIL_SHAPE = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

	private final UserService userService;
	private final CharacterStatsService characterStatsService;
	private final SupabaseAuthService supabaseAuthService;

	public UserProfileService(
			UserService userService,
			CharacterStatsService characterStatsService,
			SupabaseAuthService supabaseAuthService) {
		this.userService = userService;
		this.characterStatsService = characterStatsService;
		this.supabaseAuthService = supabaseAuthService;
	}

	public UserAccountResponse currentUser(String subject) {
		User user = userService.getByAuthSubject(subject);
		return toResponse(user, characterStatsService.getByUserId(user.getId()));
	}

	public UserAccountResponse update(String subject, UpdateUserRequest request) {
		String playerName = requiredWhenPresent(request.playerName(), "Player name is required");
		String userIcon = requiredWhenPresent(request.userIcon(), "User icon is required");
		String email = normalizeEmail(request.email());
		String password = passwordChange(request.password());

		User user = userService.getByAuthSubject(subject);
		if (playerName != null) {
			characterStatsService.ensurePlayerNameAvailable(user.getId(), playerName);
		}
		boolean emailChanged = email != null && !email.equals(user.getEmail());
		if (emailChanged) {
			userService.ensureEmailAvailable(email, user.getId());
			supabaseAuthService.updateEmail(user.getInnerId(), email);
		}

		User updated;
		try {
			updated = userService.updateAccount(subject, userIcon, emailChanged ? email : null, playerName);
		} catch (RuntimeException ex) {
			if (emailChanged) {
				restoreAuthEmail(user.getInnerId(), user.getEmail());
			}
			throw ex;
		}

		if (password != null) {
			supabaseAuthService.updatePassword(user.getInnerId(), password);
		}
		return toResponse(updated, characterStatsService.getByUserId(updated.getId()));
	}

	private void restoreAuthEmail(UUID authId, String previousEmail) {
		try {
			supabaseAuthService.updateEmail(authId, previousEmail);
		} catch (RuntimeException ex) {
			log.warn("Could not restore Auth email for {}", authId, ex);
		}
	}

	private static String requiredWhenPresent(String value, String blankMessage) {
		if (value == null) {
			return null;
		}
		if (!StringUtils.hasText(value)) {
			throw new InvalidRequestException(blankMessage);
		}
		return value.trim();
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		if (!StringUtils.hasText(email)) {
			throw new InvalidRequestException("Email is required");
		}
		String normalized = email.trim().toLowerCase();
		if (!normalized.matches(EMAIL_SHAPE)) {
			throw new InvalidRequestException("Invalid email");
		}
		return normalized;
	}

	// Blank or omitted keeps the current Auth password.
	private static String passwordChange(String password) {
		if (password == null || password.isEmpty()) {
			return null;
		}
		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw new InvalidRequestException("Password must be at least 6 characters");
		}
		return password;
	}

	private static UserAccountResponse toResponse(User user, CharacterStats stats) {
		return new UserAccountResponse(new UserAccountDto(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getUserIcon(),
				stats.getPlayerName(),
				stats.getPlayerClass().displayName()));
	}
}
