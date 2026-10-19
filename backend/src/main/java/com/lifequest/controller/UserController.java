package com.lifequest.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.UpdateUserRequest;
import com.lifequest.model.dto.UserAccountResponse;
import com.lifequest.service.UserProfileService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserProfileService userProfileService;

	public UserController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@GetMapping("/me")
	public UserAccountResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
		return userProfileService.currentUser(jwt.getSubject());
	}

	@PatchMapping("/me")
	public UserAccountResponse update(
			@AuthenticationPrincipal Jwt jwt,
			@RequestBody UpdateUserRequest request) {
		return userProfileService.update(jwt.getSubject(), request);
	}
}
