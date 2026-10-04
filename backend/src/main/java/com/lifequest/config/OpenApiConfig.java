package com.lifequest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI lifeQuestOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("LifeQuest API").version("0.0.1"))
				.addServersItem(new Server().url("/"));
	}

}
