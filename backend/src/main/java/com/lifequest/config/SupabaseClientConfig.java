package com.lifequest.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SupabaseProperties.class)
public class SupabaseClientConfig {

	@Bean
	RestClient supabaseRestClient(SupabaseProperties properties) {
		String url = properties.url() == null ? "" : properties.url().replaceAll("/+$", "");
		RestClient.Builder configured = RestClient.builder()
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + nullToEmpty(properties.serviceRoleKey()))
				.defaultHeader("apikey", nullToEmpty(properties.serviceRoleKey()));
		if (!url.isBlank()) {
			configured = configured.baseUrl(url);
		}
		return configured.build();
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
