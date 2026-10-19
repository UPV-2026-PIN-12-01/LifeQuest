package com.lifequest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	public static final String BEARER_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI lifeQuestOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("LifeQuest API").version("0.0.1"))
				.addServersItem(new Server().url("/"))
				.components(new Components().addSecuritySchemes(BEARER_SCHEME, bearerScheme()))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
	}

	private static SecurityScheme bearerScheme() {
		return new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description("Supabase access_token from POST /api/auth/login. Paste the token only.");
	}

}
