package com.lifequest.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.EquipmentRequest;
import com.lifequest.model.dto.MarketDto;
import com.lifequest.model.dto.MarketPurchaseRequest;
import com.lifequest.service.MarketService;
import com.lifequest.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/players/me/market")
public class MarketController {

	private final MarketService marketService;
	private final UserService userService;

	public MarketController(MarketService marketService, UserService userService) {
		this.marketService = marketService;
		this.userService = userService;
	}

	@GetMapping
	public MarketDto market(@AuthenticationPrincipal Jwt jwt) {
		return marketService.getMarket(userService.getIdByAuthSubject(jwt.getSubject()));
	}

	@PostMapping("/purchases")
	public MarketDto buy(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody MarketPurchaseRequest request) {
		return marketService.buy(userService.getIdByAuthSubject(jwt.getSubject()), request.catalogId());
	}

	@PutMapping("/inventory/{inventoryId}/equipment")
	public MarketDto setEquipped(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long inventoryId,
			@RequestBody EquipmentRequest request) {
		return marketService.setEquipped(
				userService.getIdByAuthSubject(jwt.getSubject()), inventoryId, request.equipped());
	}

	@PostMapping("/inventory/{inventoryId}/sales")
	public MarketDto sell(@AuthenticationPrincipal Jwt jwt, @PathVariable Long inventoryId) {
		return marketService.sell(userService.getIdByAuthSubject(jwt.getSubject()), inventoryId);
	}

	@PostMapping("/inventory/{inventoryId}/upgrades")
	public MarketDto upgrade(@AuthenticationPrincipal Jwt jwt, @PathVariable Long inventoryId) {
		return marketService.upgrade(userService.getIdByAuthSubject(jwt.getSubject()), inventoryId);
	}
}
