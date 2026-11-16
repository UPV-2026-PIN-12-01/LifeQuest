package com.lifequest.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.AchievementDto;
import com.lifequest.service.AchievementService;
import com.lifequest.service.UserService;

@RestController
@RequestMapping("/api/players/me/achievements")
public class AchievementController {
	private final AchievementService achievementService;
	private final UserService userService;

	public AchievementController(AchievementService achievementService, UserService userService) {
		this.achievementService = achievementService;
		this.userService = userService;
	}

	@GetMapping
	public List<AchievementDto> list(@AuthenticationPrincipal Jwt jwt) {
		return achievementService.findForUser(userService.getIdByAuthSubject(jwt.getSubject()));
	}

	@PostMapping("/{indexId}/claim")
	public AchievementDto claim(@AuthenticationPrincipal Jwt jwt, @PathVariable Long indexId) {
		return achievementService.claim(userService.getIdByAuthSubject(jwt.getSubject()), indexId);
	}
}
