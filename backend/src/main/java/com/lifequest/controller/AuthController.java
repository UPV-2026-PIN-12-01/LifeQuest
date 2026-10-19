package com.lifequest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import com.lifequest.model.dto.AuthenticatedUserDto;
import com.lifequest.model.dto.LoginDto;
import com.lifequest.model.dto.ProfileDto;
import com.lifequest.service.SignupService;
import com.lifequest.service.SupabaseAuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final SignupService signupService;
	private final SupabaseAuthService supabaseAuthService;

	public AuthController(SignupService signupService, SupabaseAuthService supabaseAuthService) {
		this.signupService = signupService;
		this.supabaseAuthService = supabaseAuthService;
	}

	@SecurityRequirements
	@PostMapping("/login")
	public LoginDto login(
			@RequestParam String email,
			@RequestParam String password) {
		return supabaseAuthService.login(email.trim().toLowerCase(), password);
	}

	@GetMapping("/me")
	public AuthenticatedUserDto currentUser(@AuthenticationPrincipal Jwt jwt) {
		return new AuthenticatedUserDto(jwt.getSubject(), jwt.getClaimAsString("email"));
	}

	@SecurityRequirements
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
