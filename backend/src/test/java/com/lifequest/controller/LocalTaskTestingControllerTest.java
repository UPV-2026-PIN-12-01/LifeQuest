package com.lifequest.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifequest.config.CorsConfig;
import com.lifequest.config.SecurityConfig;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.service.TaskCompletionService;
import com.lifequest.service.UserService;

@WebMvcTest(LocalTaskTestingController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
@ActiveProfiles("local")
class LocalTaskTestingControllerTest {

	private static final String AUTH_SUBJECT = "00000000-0000-0000-0000-000000000007";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskCompletionService taskCompletionService;

	@MockitoBean
	private UserService userService;

	@Test
	void advertisesTestingModeWhenLocalProfileIsActive() throws Exception {
		mockMvc.perform(get("/api/players/me/tasks/testing-mode")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enabled").value(true));
	}

	@Test
	void instantlyCompletesAuthenticatedUsersTask() throws Exception {
		TaskDto completed = new TaskDto(
				5L, "Test task", "", "📘", "blue", "2026-11-05", "10:00", "11:00",
				"completed", "low", 10L, 5L, null, "none", null, false, 0);
		when(userService.getIdByAuthSubject(AUTH_SUBJECT)).thenReturn(7L);
		when(taskCompletionService.completeInstantlyForTesting(7L, 5L)).thenReturn(completed);

		mockMvc.perform(post("/api/players/me/tasks/5/complete-instant")
				.with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("completed"));

		verify(taskCompletionService).completeInstantlyForTesting(7L, 5L);
	}
}
