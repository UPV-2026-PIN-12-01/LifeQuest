package com.lifequest.controller;

import static org.mockito.Mockito.verify;
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
import com.lifequest.exception.ResourceNotFoundException;
import com.lifequest.model.dto.FriendSummaryDto;
import com.lifequest.model.dto.FriendsOverviewDto;
import com.lifequest.service.FriendshipService;
import com.lifequest.service.UserService;

@WebMvcTest(FriendshipController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
class FriendshipControllerTest {

	private static final String AUTH_SUBJECT = "00000000-0000-0000-0000-000000000007";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FriendshipService friendshipService;

	@MockitoBean
	private UserService userService;

	@BeforeEach
	void loggedInUser() {
		when(userService.getIdByAuthSubject(AUTH_SUBJECT)).thenReturn(7L);
	}

	@Test
	void listsFriends() throws Exception {
		when(friendshipService.overview(7L)).thenReturn(new FriendsOverviewDto(
				List.of(new FriendSummaryDto(8L, "Hero-test-can-delete-later", "Caballero", "⚔️", null, 3)), List.of(), List.of()));

		mockMvc.perform(get("/api/friends").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.friends[0].playerName").value("Hero-test-can-delete-later"));
	}

	@Test
	void blankPlayerNameIsBadRequest() throws Exception {
		mockMvc.perform(post("/api/friends/requests").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT)))
				.contentType(MediaType.APPLICATION_JSON).content("{\"playerName\":\" \"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void acceptReturnsNoContent() throws Exception {
		mockMvc.perform(post("/api/friends/requests/5/accept").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isNoContent());
		verify(friendshipService).acceptRequest(7L, 5L);
	}

	@Test
	void profileOfNonFriendIsNotFound() throws Exception {
		when(friendshipService.profile(7L, 9L)).thenThrow(new ResourceNotFoundException("Friend not found"));

		mockMvc.perform(get("/api/friends/9").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isNotFound());
	}

	@Test
	void feedIsNotTakenAsAFriendId() throws Exception {
		when(friendshipService.activityFeed(7L)).thenReturn(List.of());

		mockMvc.perform(get("/api/friends/feed").with(jwt().jwt(token -> token.subject(AUTH_SUBJECT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
		verify(friendshipService).activityFeed(7L);
	}

	@Test
	void requiresLogin() throws Exception {
		mockMvc.perform(get("/api/friends")).andExpect(status().isUnauthorized());
	}
}
