package com.lifequest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

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
import com.lifequest.exception.InvalidRequestException;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.model.dto.TaskRequestDto;
import com.lifequest.service.TaskService;
import com.lifequest.service.UserService;

@WebMvcTest(TaskController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
class TaskControllerTest {

	private static final String AUTH_SUBJECT = "00000000-0000-0000-0000-000000000007";
	private static final String BODY = """
			{"title":"Repasar test-can-delete-later","description":"","icon":"📘","color":"blue",
			"date":"2026-10-12","startTime":"09:00","endTime":"10:30","priority":"high"}
			""";
	private static final TaskRequestDto REQUEST = new TaskRequestDto("Repasar test-can-delete-later", "", "📘", "blue",
			LocalDate.parse("2026-10-12"), LocalTime.parse("09:00"), LocalTime.parse("10:30"), "high");
	private static final TaskDto SAVED = new TaskDto(5L, "Repasar test-can-delete-later", "", "📘", "blue",
			"2026-10-12", "09:00", "10:30", "pending", "high", 30);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private UserService userService;

	@BeforeEach
	void loggedInUser() {
		when(userService.getIdByAuthSubject(AUTH_SUBJECT)).thenReturn(7L);
	}

	@Test
	void createsTask() throws Exception {
		when(taskService.create(7L, REQUEST)).thenReturn(SAVED);

		mockMvc.perform(post("/api/players/me/tasks").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.xp").value(30));
	}

	@Test
	void updatesTask() throws Exception {
		when(taskService.update(7L, 5L, REQUEST)).thenReturn(SAVED);

		mockMvc.perform(put("/api/players/me/tasks/5").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Repasar test-can-delete-later"));
	}

	@Test
	void deletesTask() throws Exception {
		mockMvc.perform(delete("/api/players/me/tasks/5").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isNoContent());

		verify(taskService).delete(7L, 5L);
	}

	@Test
	void invalidFormIsBadRequest() throws Exception {
		when(taskService.create(eq(7L), any())).thenThrow(new InvalidRequestException("endTime must be after startTime"));

		mockMvc.perform(post("/api/players/me/tasks").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("endTime must be after startTime"));
	}

	@Test
	void missingTaskIsNotFound() throws Exception {
		doThrow(new ResourceNotFoundException("Task not found")).when(taskService).delete(7L, 9L);

		mockMvc.perform(delete("/api/players/me/tasks/9").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isNotFound());
	}

	@Test
	void withoutTokenIsUnauthorized() throws Exception {
		mockMvc.perform(delete("/api/players/me/tasks/5"))
				.andExpect(status().isUnauthorized());
	}
}
