package com.lifequest.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifequest.config.CorsConfig;
import com.lifequest.config.SecurityConfig;
import com.lifequest.model.entity.CharacterStats;
import com.lifequest.service.CharacterStatsService;
import com.lifequest.service.UserService;

@WebMvcTest(CharacterController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
class CharacterControllerTest {

	private static final String AUTH_SUBJECT = "00000000-0000-0000-0000-000000000007";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CharacterStatsService characterStatsService;

	@MockitoBean
	private UserService userService;

	@Test
	void addsStatsToTheAuthenticatedCharacter() throws Exception {
		CharacterStats stats = new CharacterStats();
		stats.setHp(22L);
		stats.setStrength(8);
		when(userService.getIdByAuthSubject(AUTH_SUBJECT)).thenReturn(7L);
		when(characterStatsService.increaseCharacterStats(7L, 2, 3, 0, 1, 0)).thenReturn(stats);

		mockMvc.perform(post("/api/character/stats").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"hp":2,"strength":3,"speed":0,"stamina":1,"wisdom":0}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.hp").value(22))
				.andExpect(jsonPath("$.strength").value(8));

		verify(characterStatsService).increaseCharacterStats(7L, 2, 3, 0, 1, 0);
	}

	@Test
	void rejectsNegativeStatIncrements() throws Exception {
		mockMvc.perform(post("/api/character/stats").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"hp":-1,"strength":0,"speed":0,"stamina":0,"wisdom":0}
						"""))
				.andExpect(status().isBadRequest());

		verify(characterStatsService, never()).increaseCharacterStats(7L, -1, 0, 0, 0, 0);
	}
}
