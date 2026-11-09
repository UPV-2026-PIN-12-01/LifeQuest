package com.lifequest.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.ChatMessageRequest;
import com.lifequest.model.dto.ChatReplyResponse;
import com.lifequest.model.dto.ChatThreadDetail;
import com.lifequest.model.dto.ChatThreadSummary;
import com.lifequest.service.ChatService;
import com.lifequest.service.UserService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatService chatService;
	private final UserService userService;

	public ChatController(ChatService chatService, UserService userService) {
		this.chatService = chatService;
		this.userService = userService;
	}

	@GetMapping("/threads")
	public List<ChatThreadSummary> threads(@AuthenticationPrincipal Jwt jwt) {
		return chatService.list(userId(jwt));
	}

	@GetMapping("/threads/{threadId}")
	public ChatThreadDetail thread(@AuthenticationPrincipal Jwt jwt, @PathVariable Long threadId) {
		return chatService.get(userId(jwt), threadId);
	}

	@PostMapping("/messages")
	public ChatReplyResponse messages(@AuthenticationPrincipal Jwt jwt, @RequestBody ChatMessageRequest request) {
		return chatService.reply(userId(jwt), request);
	}

	private Long userId(Jwt jwt) {
		return userService.getIdByAuthSubject(jwt.getSubject());
	}
}
