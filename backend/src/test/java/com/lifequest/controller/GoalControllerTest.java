package com.lifequest.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifequest.config.CorsConfig;
import com.lifequest.config.SecurityConfig;
import com.lifequest.model.dto.GoalDto;
import com.lifequest.model.dto.GoalQuotaDto;
import com.lifequest.service.GoalService;
import com.lifequest.service.UserService;

@WebMvcTest(GoalController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
class GoalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GoalService goalService;

	@MockitoBean
	private UserService userService;

	private static final String AUTH_SUBJECT = "00000000-0000-0000-0000-000000000007";

	@BeforeEach
	void loggedInUser() {
		when(userService.getIdByAuthSubject(AUTH_SUBJECT)).thenReturn(7L);
	}

	@Test
	void returnsActiveGoalsForAuthenticatedUser() throws Exception {
		GoalDto goal = new GoalDto(4L, "Leer un libro", "Terminar el libro elegido",
				Instant.parse("2026-11-01T10:00:00Z"),
				LocalDateTime.parse("2026-11-01T10:00:00"),
				LocalDateTime.parse("2026-11-30T10:00:00"),
				false, null, "pending", 1, 100, 50,
				Instant.parse("2026-12-01T10:00:00Z"), false);
		when(goalService.findForUser(7L, false)).thenReturn(List.of(goal));

		mockMvc.perform(get("/api/players/me/goals")
				.param("history", "false")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(4))
				.andExpect(jsonPath("$[0].title").value("Leer un libro"))
				.andExpect(jsonPath("$[0].status").value("pending"))
				.andExpect(jsonPath("$[0].xpReward").value(100))
				.andExpect(jsonPath("$[0].rewardTier").value(1));

		verify(goalService).findForUser(7L, false);
	}

	@Test
	void returnsWeeklyQuota() throws Exception {
		when(goalService.getQuota(7L)).thenReturn(new GoalQuotaDto(2, 3));

		mockMvc.perform(get("/api/players/me/goals/quota")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.createdThisWeek").value(2))
				.andExpect(jsonPath("$.weeklyLimit").value(3));
	}

	@Test
	void withoutTokenIsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/players/me/goals"))
				.andExpect(status().isUnauthorized());
	}
}
