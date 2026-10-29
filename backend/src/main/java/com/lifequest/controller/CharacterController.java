package com.lifequest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.model.dto.CharacterStatsDto;
import com.lifequest.service.CharacterStatsService;
import com.lifequest.service.UserService;

@RestController
@RequestMapping("/api/character")
public class CharacterController {

	private final CharacterStatsService characterStatsService;
	private final UserService userService;

	public CharacterController(CharacterStatsService characterStatsService, UserService userService) {
		this.characterStatsService = characterStatsService;
		this.userService = userService;
	}

	@GetMapping("/stats")
	public ResponseEntity<CharacterStats> getCharacterStats(@AuthenticationPrincipal Jwt jwt) {
		Long userId = userService.getIdByAuthSubject(jwt.getSubject());
		return ResponseEntity.ok(characterStatsService.getByUserId(userId));
	}

	@PostMapping("/stats")
	public ResponseEntity<CharacterStats> increaseCharacterStats(
			@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CharacterStatsDto statsDto) {
		Long userId = userService.getIdByAuthSubject(jwt.getSubject());
		CharacterStats stats = characterStatsService.increaseCharacterStats(userId, statsDto.hp(),
				statsDto.strength(), statsDto.speed(), statsDto.stamina(), statsDto.wisdom());

		return ResponseEntity.ok(stats);
	}
}