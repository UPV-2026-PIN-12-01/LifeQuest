package com.lifequest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifequest.config.CorsConfig;
import com.lifequest.config.SecurityConfig;
import com.lifequest.model.dto.ChallengeDto;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.service.ChallengeService;
import com.lifequest.service.UserService;

@WebMvcTest(ChallengeController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
class ChallengeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChallengeService challengeService;

	@MockitoBean
	private UserService userService;

	private static final String AUTH_SUBJECT = "00000000-0000-0000-0000-000000000007";

	@BeforeEach
	void loggedInUser() {
		when(userService.getIdByAuthSubject(AUTH_SUBJECT)).thenReturn(7L);
	}

	@Test
	void returnsActiveChallenges() throws Exception {
		TaskDto task = new TaskDto(12L, "Estudiar test-can-delete-later", "", "📘", "purple",
				"2026-10-28", "09:00", "10:30", "finished", "medium", 20, "none", null, false, 0);
		ChallengeDto challenge = new ChallengeDto(4L, null, "No usar solucionario", "Resolver sin soluciones",
				"HARD", false, 12L, null, "SHOWN", "No consultar las soluciones", 75, 25, false, task);
		when(challengeService.findForUser(7L, false)).thenReturn(List.of(challenge));

		mockMvc.perform(get("/api/players/me/challenges")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("No usar solucionario"))
				.andExpect(jsonPath("$[0].difficulty").value("HARD"))
				.andExpect(jsonPath("$[0].task.title").value("Estudiar test-can-delete-later"));
	}

	@Test
	void createsChallenge() throws Exception {
		ChallengeDto challenge = new ChallengeDto(4L, null, "No usar solucionario", "",
				"HARD", false, 12L, null, "SHOWN", "", 75, 25, false, null);
		when(challengeService.create(7L, new com.lifequest.model.dto.ChallengeRequestDto(
				"No usar solucionario", "", "HARD", 12L, ""))).thenReturn(challenge);

		mockMvc.perform(post("/api/players/me/challenges")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"No usar solucionario","description":"","difficulty":"HARD","taskId":12,"requirement":""}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(4))
				.andExpect(jsonPath("$.xp").value(75));
	}

	@Test
	void withoutTokenIsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/players/me/challenges"))
				.andExpect(status().isUnauthorized());
	}
}
