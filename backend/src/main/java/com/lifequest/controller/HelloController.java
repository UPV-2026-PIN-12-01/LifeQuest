package com.lifequest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api")
public class HelloController {

	@SecurityRequirements
	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("ok");
	}

	@SecurityRequirements
	@GetMapping("/hello")
	public HelloResponse hello() {
		return new HelloResponse("Hello from LifeQuest");
	}

	public record HealthResponse(String status) {
	}

	public record HelloResponse(String message) {
	}

}
