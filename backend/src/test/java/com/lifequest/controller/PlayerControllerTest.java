package com.lifequest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifequest.config.CorsConfig;
import com.lifequest.config.SecurityConfig;
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.PlayerProfileDto;
import com.lifequest.model.dto.TaskDto;
import com.lifequest.service.PlayerService;
import com.lifequest.service.TaskService;

@WebMvcTest(PlayerController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
class PlayerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PlayerService playerService;

	@MockitoBean
	private TaskService taskService;

	@Test
	void returnsWeekTasks() throws Exception {
		when(taskService.findForUser(7L, LocalDate.parse("2026-10-05"), LocalDate.parse("2026-10-11")))
				.thenReturn(List.of(new TaskDto(1L, "Repasar test-can-delete-later", "📘", "purple",
						"2026-10-05", "09:00", "10:30", "completed", "medium", 50)));

		mockMvc.perform(get("/api/players/7/tasks").param("from", "2026-10-05").param("to", "2026-10-11"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Repasar test-can-delete-later"))
				.andExpect(jsonPath("$[0].startTime").value("09:00"))
				.andExpect(jsonPath("$[0].xp").value(50));
	}

	@Test
	void badDateIsBadRequest() throws Exception {
		mockMvc.perform(get("/api/players/7/tasks").param("from", "lunes").param("to", "2026-10-11"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returnsProfile() throws Exception {
		when(playerService.getProfile(7L))
				.thenReturn(new PlayerProfileDto(7L, "test-can-delete-later", "Hero", "Mago", 3, 4, 17, 2));

		mockMvc.perform(get("/api/players/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.playerClass").value("Mago"))
				.andExpect(jsonPath("$.xpForNextLevel").value(17));
	}

	@Test
	void unknownUserIsNotFound() throws Exception {
		when(playerService.getProfile(99L)).thenThrow(new ResourceNotFoundException("User not found"));

		mockMvc.perform(get("/api/players/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("User not found"));
	}
}
