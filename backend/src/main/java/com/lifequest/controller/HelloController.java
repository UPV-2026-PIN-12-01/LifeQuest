package com.lifequest.controller;

import java.net.URI;

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

		String url = System.getenv("SUPABASE_URL");

System.out.println("SUPABASE_URL = [" + url + "]");

if (url == null || url.isBlank()) {
    throw new IllegalStateException(
        "SUPABASE_URL no está definida"
    );
}

url = url.trim();

URI uri = URI.create(url);

System.out.println("Host utilizado = [" + uri.getHost() + "]");

		return new HelloResponse("Hello from LifeQuest");
	}

	public record HealthResponse(String status) {
	}

	public record HelloResponse(String message) {
	}

}
