package com.lifequest.service.context;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.lifequest.model.dto.PlayerProfileDto;
import com.lifequest.service.PlayerService;

@Component
@Order(1)
public class ProfileChatContext implements ChatContextContributor {

	private final PlayerService playerService;

	public ProfileChatContext(PlayerService playerService) {
		this.playerService = playerService;
	}

	@Override
	public String text(Long userId) {
		PlayerProfileDto profile = playerService.getProfile(userId);
		return "Perfil: " + text(profile.playerName()) + ", username " + text(profile.username()) + ", "
				+ text(profile.playerClass()) + ", nivel " + profile.level() + ", racha " + profile.streakDays() + ".";
	}

	private static String text(String value) {
		return value == null || value.isBlank() ? "" : value.trim();
	}
}
