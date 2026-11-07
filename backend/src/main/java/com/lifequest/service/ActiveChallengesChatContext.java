package com.lifequest.service;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.lifequest.model.dto.ChallengeDto;

@Component
@Order(4)
public class ActiveChallengesChatContext implements ChatContextContributor {

	private final ChallengeService challengeService;

	public ActiveChallengesChatContext(ChallengeService challengeService) {
		this.challengeService = challengeService;
	}

	@Override
	public String text(Long userId) {
		List<String> lines = challengeService.findForUser(userId, false).stream()
				.map(challenge -> challenge.name() == null ? "" : challenge.name())
				.toList();
		return ChatContextContributor.list("Desafíos activos:", lines);
	}
}
