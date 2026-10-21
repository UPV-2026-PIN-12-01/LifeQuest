package com.lifequest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.lifequest.model.entity.CharacterStats;
import com.lifequest.service.CharacterStatsService;
import com.lifequest.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/character")
public class CharacterController {

    private final CharacterStatsService characterStatsService;
    private final UserService userService;

    public CharacterController(CharacterStatsService characterStatsService, UserService userService) {
        this.characterStatsService = characterStatsService;
        this.userService = userService;
    }

    @GetMapping("/stats")
    public ResponseEntity<CharacterStats> getCharacterStats(@AuthenticationPrincipal Jwt jwt) {
        UUID authId = UUID.fromString(jwt.getSubject());
        Long userId = userService.getByInnerId(authId).getId();
        return ResponseEntity.ok(characterStatsService.getByUserId(userId));
    }
}