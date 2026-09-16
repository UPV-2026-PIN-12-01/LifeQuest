package com.lifequest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("ok");
	}

	@GetMapping("/hello")
	public HelloResponse hello() {
		return new HelloResponse("Hello from LifeQuest");
	}

	public record HealthResponse(String status) {
	}

	public record HelloResponse(String message) {
	}

}
