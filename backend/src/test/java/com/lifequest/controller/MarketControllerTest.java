package com.lifequest.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifequest.config.CorsConfig;
import com.lifequest.config.SecurityConfig;
import com.lifequest.model.dto.MarketDto;
import com.lifequest.model.dto.MarketItemDto;
import com.lifequest.service.MarketService;
import com.lifequest.service.UserService;

@WebMvcTest(MarketController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
class MarketControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MarketService marketService;

	@MockitoBean
	private UserService userService;

	private static final String AUTH_SUBJECT = "00000000-0000-0000-0000-000000000007";

	@BeforeEach
	void loggedInUser() {
		when(userService.getIdByAuthSubject(AUTH_SUBJECT)).thenReturn(7L);
	}

	@Test
	void returnsCatalogInventoryAndBalanceForTheLoggedInPlayer() throws Exception {
		when(marketService.getMarket(7L)).thenReturn(new MarketDto(
				List.of(new MarketItemDto("sword", "sword", "Espada del Alba", "⚔️", "Arma",
						"Rare", 3, 100, 55, List.of("Fuerza +8"), null, null, 260, false, 1)),
				List.of(), Map.of("strength", 8L), 500L));

		mockMvc.perform(get("/api/players/me/market")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.shopItems[0].catalogId").value("sword"))
				.andExpect(jsonPath("$.inventory").isEmpty())
				.andExpect(jsonPath("$.objectStats.strength").value(8))
				.andExpect(jsonPath("$.gold").value(500));
	}

	@Test
	void purchaseUsesTheAuthenticatedPlayerAndCatalogIdentifier() throws Exception {
		when(marketService.buy(7L, "sword")).thenReturn(new MarketDto(List.of(), List.of(), Map.of(), 240L));

		mockMvc.perform(post("/api/players/me/market/purchases")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType("application/json")
				.content("{\"catalogId\":\"sword\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.gold").value(240));

		verify(marketService).buy(7L, "sword");
	}

	@Test
	void duplicateEquippedSlotReturnsAReadableConflict() throws Exception {
		when(marketService.setEquipped(7L, 15L, true))
				.thenThrow(new DataIntegrityViolationException(
						"duplicate key violates player_inventory_one_equipped_item_per_slot"));

		mockMvc.perform(put("/api/players/me/market/inventory/15/equipment")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType("application/json")
				.content("{\"equipped\":true}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value(
						"Ya hay un objeto equipado en ese espacio. Actualiza el inventario e inténtalo de nuevo."));
	}
}
