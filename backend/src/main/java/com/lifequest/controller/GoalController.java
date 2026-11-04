package com.lifequest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.GoalDto;
import com.lifequest.model.dto.GoalEvaluationRequestDto;
import com.lifequest.model.dto.GoalQuotaDto;
import com.lifequest.model.dto.GoalRequestDto;
import com.lifequest.service.GoalService;
import com.lifequest.service.UserService;

@RestController
@RequestMapping("/api/players/me/goals")
public class GoalController {

	private final GoalService goalService;
	private final UserService userService;

	public GoalController(GoalService goalService, UserService userService) {
		this.goalService = goalService;
		this.userService = userService;
	}

	@GetMapping
	public List<GoalDto> list(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "false") boolean history) {
		return goalService.findForUser(userService.getIdByAuthSubject(jwt.getSubject()), history);
	}

	@GetMapping("/quota")
	public GoalQuotaDto quota(@AuthenticationPrincipal Jwt jwt) {
		return goalService.getQuota(userService.getIdByAuthSubject(jwt.getSubject()));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public GoalDto create(@AuthenticationPrincipal Jwt jwt, @RequestBody GoalRequestDto request) {
		return goalService.create(userService.getIdByAuthSubject(jwt.getSubject()), request);
	}

	@PatchMapping("/{goalId}/evaluate")
	public GoalDto evaluate(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long goalId,
			@RequestBody GoalEvaluationRequestDto request) {
		return goalService.evaluate(userService.getIdByAuthSubject(jwt.getSubject()), goalId, request);
	}

	@PostMapping("/{goalId}/claim")
	public GoalDto claim(@AuthenticationPrincipal Jwt jwt, @PathVariable Long goalId) {
		return goalService.claim(userService.getIdByAuthSubject(jwt.getSubject()), goalId);
	}

	@DeleteMapping("/{goalId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long goalId) {
		goalService.delete(userService.getIdByAuthSubject(jwt.getSubject()), goalId);
	}
}
