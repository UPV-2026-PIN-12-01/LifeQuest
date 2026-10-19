package com.lifequest.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
	// Keep MAX_PHOTO_BYTES and photo types in sync with SignupService.java
	private static final long MAX_PHOTO_BYTES = 2 * 1024 * 1024;
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

	public UserProfileService(
			UserService userService,
			CharacterStatsService characterStatsService,
			SupabaseAuthService supabaseAuthService,
			SupabaseStorageService supabaseStorageService) {
		this.userService = userService;
		this.characterStatsService = characterStatsService;
		this.supabaseAuthService = supabaseAuthService;
		this.supabaseStorageService = supabaseStorageService;
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

	public UserAccountResponse updatePhoto(String subject, MultipartFile photo) {
		PhotoPayload payload = resolvePhoto(photo);
		User user = userService.getByAuthSubject(subject);
		String profileUrl = supabaseStorageService.uploadAvatar(
				user.getInnerId(), payload.bytes(), payload.contentType(), payload.objectName());
		String versionedUrl = profileUrl + "?v=" + System.currentTimeMillis();
		userService.updateProfileUrl(user.getId(), versionedUrl);
		user.setProfileUrl(versionedUrl);
		return toResponse(user, characterStatsService.getByUserId(user.getId()));
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
				stats.getPlayerClass().displayName(),
				user.getProfileUrl()));
	}

	private static PhotoPayload resolvePhoto(MultipartFile photo) {
		if (photo == null || photo.isEmpty()) {
			throw new InvalidRequestException("Photo is required");
		}
		if (photo.getSize() > MAX_PHOTO_BYTES) {
			throw new InvalidRequestException("Photo must be jpeg, png, webp, or gif and 2MB or smaller");
		}
		String contentType = resolveContentType(photo);
		if (contentType == null) {
			throw new InvalidRequestException("Photo must be jpeg, png, webp, or gif and 2MB or smaller");
		}
		byte[] bytes;
		try {
			bytes = photo.getBytes();
		} catch (IOException ex) {
			throw new InvalidRequestException("Invalid photo");
		}
		if (bytes.length > MAX_PHOTO_BYTES) {
			throw new InvalidRequestException("Photo must be jpeg, png, webp, or gif and 2MB or smaller");
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
