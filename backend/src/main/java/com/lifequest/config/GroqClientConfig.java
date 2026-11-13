package com.lifequest.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GroqProperties.class)
public class GroqClientConfig {

	private static final Duration TIMEOUT = Duration.ofSeconds(20);

	@Bean
	RestClient groqRestClient(GroqProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(TIMEOUT);
		return RestClient.builder()
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + nullToEmpty(properties.apiKey()))
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
