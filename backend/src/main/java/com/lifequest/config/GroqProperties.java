package com.lifequest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lifequest.groq")
public record GroqProperties(String apiKey, String model, String url) {
}
