package com.lifequest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.ChallengeDto;
import com.lifequest.model.dto.ChallengeEvaluationRequestDto;
import com.lifequest.model.dto.ChallengeRequestDto;
import com.lifequest.service.ChallengeService;
import com.lifequest.service.UserService;

@RestController
@RequestMapping("/api/players/me/challenges")
public class ChallengeController {

	private final ChallengeService challengeService;
	private final UserService userService;

	public ChallengeController(ChallengeService challengeService, UserService userService) {
		this.challengeService = challengeService;
		this.userService = userService;
	}

	@GetMapping
	public List<ChallengeDto> list(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "false") boolean history) {
		return challengeService.findForUser(userService.getIdByAuthSubject(jwt.getSubject()), history);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ChallengeDto create(
			@AuthenticationPrincipal Jwt jwt,
			@RequestBody ChallengeRequestDto request) {
		return challengeService.create(userService.getIdByAuthSubject(jwt.getSubject()), request);
	}

	@PostMapping("/{challengeId}/evaluate")
	public ChallengeDto evaluate(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long challengeId,
			@RequestBody ChallengeEvaluationRequestDto request) {
		return challengeService.evaluate(userService.getIdByAuthSubject(jwt.getSubject()), challengeId, request);
	}

	@PostMapping("/{challengeId}/claim")
	public ChallengeDto claim(@AuthenticationPrincipal Jwt jwt, @PathVariable Long challengeId) {
		return challengeService.claim(userService.getIdByAuthSubject(jwt.getSubject()), challengeId);
	}

	@DeleteMapping("/{challengeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long challengeId) {
		challengeService.delete(userService.getIdByAuthSubject(jwt.getSubject()), challengeId);
	}
}
