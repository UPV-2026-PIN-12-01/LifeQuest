package com.lifequest.service.context;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.lifequest.model.dto.DailyQuestDto;
import com.lifequest.service.PlayerService;

@Component
@Order(3)
public class DailyQuestsChatContext implements ChatContextContributor {

	private final PlayerService playerService;

	public DailyQuestsChatContext(PlayerService playerService) {
		this.playerService = playerService;
	}

	@Override
	public String text(Long userId) {
		List<String> lines = playerService.getDailyQuests(userId).stream()
				.map(DailyQuestsChatContext::line)
				.toList();
		return ChatContextContributor.list("Misiones de hoy:", lines);
	}

	private static String line(DailyQuestDto quest) {
		return quest.title() + " " + quest.progress() + "/" + quest.goal();
	}
}
