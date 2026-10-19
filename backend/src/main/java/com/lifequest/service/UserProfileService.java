package com.lifequest.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lifequest.exception.InvalidRequestException;
import com.lifequest.model.dto.UpdateUserRequest;
import com.lifequest.model.dto.UserAccountDto;
import com.lifequest.model.dto.UserAccountResponse;
import com.lifequest.model.entity.User;

@Service
public class UserProfileService {

	private final UserService userService;

	public UserProfileService(UserService userService) {
		this.userService = userService;
	}

	public UserAccountResponse currentUser(String subject) {
		return toResponse(userService.getByAuthSubject(subject));
	}

	public UserAccountResponse update(String subject, UpdateUserRequest request) {
		String username = requiredWhenPresent(request.username(), "Username is required");
		String userIcon = requiredWhenPresent(request.userIcon(), "User icon is required");
		User updated = userService.updateAccount(subject, username, userIcon);
		return toResponse(updated);
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

	private static UserAccountResponse toResponse(User user) {
		return new UserAccountResponse(new UserAccountDto(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getUserIcon()));
	}
}
