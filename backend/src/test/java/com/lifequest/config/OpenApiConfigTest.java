package com.lifequest.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void swaggerHtmlRedirectsToUi() throws Exception {
		mockMvc.perform(get("/swagger.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/swagger-ui/index.html"));
	}

	@Test
	void openApiDocsAreAvailable() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.info.title").value("LifeQuest API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.security[0].bearerAuth").isArray())
				.andExpect(jsonPath("$.paths['/api/auth/me'].get.security").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty())
				.andExpect(jsonPath("$.paths['/api/auth/signup'].post.security").isEmpty());
	}

}
