package com.lifequest.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.ChatMessageRequest;
import com.lifequest.model.dto.ChatReplyResponse;
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

	@PostMapping("/messages")
	public ChatReplyResponse messages(@AuthenticationPrincipal Jwt jwt, @RequestBody ChatMessageRequest request) {
		return chatService.reply(userService.getIdByAuthSubject(jwt.getSubject()), request);
	}
}
