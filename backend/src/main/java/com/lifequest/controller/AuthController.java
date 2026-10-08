package com.lifequest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lifequest.model.dto.ProfileDto;
import com.lifequest.service.SignupService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final SignupService signupService;

	public AuthController(SignupService signupService) {
		this.signupService = signupService;
	}

	@PostMapping("/signup")
	public ResponseEntity<ProfileDto> signup(
			@RequestParam String username,
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam(required = false) String playerClass,
			@RequestParam(required = false) String userIcon,
			@RequestParam(required = false) String playerName,
			@RequestParam(required = false) MultipartFile photo) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(signupService.signup(username, email, password, playerClass, userIcon, playerName, photo));
	}
}
