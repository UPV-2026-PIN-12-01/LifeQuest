package com.lifequest.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
	private static final long MAX_PHOTO_BYTES = 2 * 1024 * 1024;
	private static final String EMAIL_SHAPE = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
	private static final Map<String, String> PHOTO_EXTENSIONS = Map.of(
			"image/jpeg", "jpg",
			"image/jpg", "jpg",
			"image/png", "png",
			"image/webp", "webp",
			"image/gif", "gif");

	private final UserService userService;
	private final CharacterStatsService characterStatsService;
	private final SupabaseAuthService supabaseAuthService;
	private final SupabaseStorageService supabaseStorageService;
	private final TransactionTemplate transactionTemplate;

	public SignupService(
			UserService userService,
			CharacterStatsService characterStatsService,
			SupabaseAuthService supabaseAuthService,
			SupabaseStorageService supabaseStorageService,
			PlatformTransactionManager transactionManager) {
		this.userService = userService;
		this.characterStatsService = characterStatsService;
		this.supabaseAuthService = supabaseAuthService;
		this.supabaseStorageService = supabaseStorageService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public ProfileDto signup(
			String username,
			String email,
			String password,
			String playerClass,
			String userIcon,
			MultipartFile photo) {
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
		PhotoPayload photoPayload = resolvePhoto(photo);

		if (userService.existsByUsername(trimmedUsername) || userService.existsByEmail(normalizedEmail)) {
			throw new DuplicateUserException("Username or email already exists");
		}

		UUID authId = supabaseAuthService.createUser(normalizedEmail, password);
		User created;
		try {
			created = transactionTemplate.execute(status -> {
				User user = userService.create(trimmedUsername, normalizedEmail, authId, icon);
				CharacterStats stats = characterStatsService.createForUser(user.getId(), trimmedUsername, resolvedClass);
				return userService.linkCharacter(user, stats);
			});
		} catch (RuntimeException ex) {
			rollbackAuth(authId);
			if (ex instanceof DataIntegrityViolationException) {
				throw new DuplicateUserException("Username or email already exists");
			}
			throw ex;
		}

		if (photoPayload != null && created != null) {
			storePhoto(created, photoPayload);
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

	private void storePhoto(User user, PhotoPayload photo) {
		try {
			String profileUrl = supabaseStorageService.uploadAvatar(
					user.getInnerId(), photo.bytes(), photo.contentType(), photo.objectName());
			userService.updateProfileUrl(user.getId(), profileUrl);
		} catch (RuntimeException ex) {
			log.warn("Could not store profile photo for {}; profile_url stays null", user.getInnerId(), ex);
		}
	}

	private static PhotoPayload resolvePhoto(MultipartFile photo) {
		if (photo == null || photo.isEmpty()) {
			return null;
		}
		if (photo.getSize() > MAX_PHOTO_BYTES) {
			throw new InvalidSignupException("Photo must be jpeg, png, webp, or gif and 2MB or smaller");
		}
		String contentType = resolveContentType(photo);
		if (contentType == null) {
			throw new InvalidSignupException("Photo must be jpeg, png, webp, or gif and 2MB or smaller");
		}
		byte[] bytes;
		try {
			bytes = photo.getBytes();
		} catch (IOException ex) {
			throw new InvalidSignupException("Invalid photo");
		}
		if (bytes.length > MAX_PHOTO_BYTES) {
			throw new InvalidSignupException("Photo must be jpeg, png, webp, or gif and 2MB or smaller");
		}
		return new PhotoPayload(bytes, contentType, "avatar." + PHOTO_EXTENSIONS.get(contentType));
	}

	private static String resolveContentType(MultipartFile photo) {
		String contentType = canonicalContentType(photo.getContentType());
		if (contentType != null) {
			return contentType;
		}
		return canonicalContentType(inferContentType(photo.getOriginalFilename()));
	}

	private static String canonicalContentType(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String type = raw.trim().toLowerCase(Locale.ROOT);
		int separator = type.indexOf(';');
		if (separator >= 0) {
			type = type.substring(0, separator).trim();
		}
		if ("image/jpg".equals(type)) {
			type = "image/jpeg";
		}
		return PHOTO_EXTENSIONS.containsKey(type) ? type : null;
	}

	private static String inferContentType(String filename) {
		if (!StringUtils.hasText(filename)) {
			return null;
		}
		String lower = filename.trim().toLowerCase(Locale.ROOT);
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if (lower.endsWith(".png")) {
			return "image/png";
		}
		if (lower.endsWith(".webp")) {
			return "image/webp";
		}
		if (lower.endsWith(".gif")) {
			return "image/gif";
		}
		return null;
	}

	private record PhotoPayload(byte[] bytes, String contentType, String objectName) {
	}
}
