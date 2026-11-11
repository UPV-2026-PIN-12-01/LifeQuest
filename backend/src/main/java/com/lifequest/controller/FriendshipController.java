package com.lifequest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.AddFriendRequestDto;
import com.lifequest.model.dto.FriendActivityDto;
import com.lifequest.model.dto.FriendProfileDto;
import com.lifequest.model.dto.FriendRequestDto;
import com.lifequest.model.dto.FriendsOverviewDto;
import com.lifequest.service.FriendshipService;
import com.lifequest.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

	private final FriendshipService friendshipService;
	private final UserService userService;

	public FriendshipController(FriendshipService friendshipService, UserService userService) {
		this.friendshipService = friendshipService;
		this.userService = userService;
	}

	@GetMapping
	public FriendsOverviewDto overview(@AuthenticationPrincipal Jwt jwt) {
		return friendshipService.overview(userId(jwt));
	}

	@PostMapping("/requests")
	@ResponseStatus(HttpStatus.CREATED)
	public FriendRequestDto sendRequest(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddFriendRequestDto request) {
		return friendshipService.sendRequest(userId(jwt), request.playerName());
	}

	@PostMapping("/requests/{requestId}/accept")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void acceptRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable Long requestId) {
		friendshipService.acceptRequest(userId(jwt), requestId);
	}

	@DeleteMapping("/requests/{requestId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRequest(@AuthenticationPrincipal Jwt jwt, @PathVariable Long requestId) {
		friendshipService.deleteRequest(userId(jwt), requestId);
	}

	@GetMapping("/feed")
	public List<FriendActivityDto> feed(@AuthenticationPrincipal Jwt jwt) {
		return friendshipService.activityFeed(userId(jwt));
	}

	@GetMapping("/{friendUserId:\\d+}")
	public FriendProfileDto profile(@AuthenticationPrincipal Jwt jwt, @PathVariable Long friendUserId) {
		return friendshipService.profile(userId(jwt), friendUserId);
	}

	@DeleteMapping("/{friendUserId:\\d+}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeFriend(@AuthenticationPrincipal Jwt jwt, @PathVariable Long friendUserId) {
		friendshipService.removeFriend(userId(jwt), friendUserId);
	}

	private Long userId(Jwt jwt) {
		return userService.getIdByAuthSubject(jwt.getSubject());
	}
}
