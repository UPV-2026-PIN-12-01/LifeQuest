package com.lifequest.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.lifequest.exception.DuplicateUserException;
import com.lifequest.exception.InvalidLoginException;
import com.lifequest.model.dto.LoginDto;

@Service
public class SupabaseAuthService {

	private static final Logger log = LoggerFactory.getLogger(SupabaseAuthService.class);

	private final RestClient restClient;

	public SupabaseAuthService(@Qualifier("supabaseRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	public UUID createUser(String email, String password) {
		try {
			AuthUser created = restClient.post()
					.uri("/auth/v1/admin/users")
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"email", email,
							"password", password,
							"email_confirm", true))
					.retrieve()
					.body(AuthUser.class);
			if (created == null || created.id() == null || created.id().isBlank()) {
				throw new IllegalStateException("Could not create auth user");
			}
			return UUID.fromString(created.id());
		} catch (RestClientResponseException ex) {
			int status = ex.getStatusCode().value();
			String body = ex.getResponseBodyAsString();
			if (status == 409 || status == 422 || (body != null && body.toLowerCase().contains("already"))) {
				throw new DuplicateUserException("Email already exists");
			}
			log.warn("Auth create failed with status {}", status);
			throw new IllegalStateException("Could not create auth user");
		}
	}

	public LoginDto login(String email, String password) {
		try {
			AuthSession session = restClient.post()
					.uri(uriBuilder -> uriBuilder
							.path("/auth/v1/token")
							.queryParam("grant_type", "password")
							.build())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"email", email,
							"password", password))
					.retrieve()
					.body(AuthSession.class);
			if (session == null || session.accessToken() == null || session.accessToken().isBlank()) {
				throw new InvalidLoginException("Invalid email or password");
			}
			return new LoginDto(
					session.accessToken(),
					session.refreshToken(),
					session.expiresIn(),
					session.tokenType());
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
				throw new InvalidLoginException("Invalid email or password");
			}
			log.warn("Auth login failed with status {}", ex.getStatusCode().value());
			throw new IllegalStateException("Could not log in");
		}
	}

	public void updateEmail(UUID authId, String email) {
		putUser(authId, Map.of("email", email, "email_confirm", true), true);
	}

	public void updatePassword(UUID authId, String password) {
		putUser(authId, Map.of("password", password), false);
	}

	public void deleteUser(UUID authId) {
		try {
			restClient.delete()
					.uri("/auth/v1/admin/users/{id}", authId)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() != 404) {
				log.warn("Auth delete failed with status {}", ex.getStatusCode().value());
			}
		} catch (RuntimeException ex) {
			log.warn("Auth delete failed for {}", authId, ex);
		}
	}

	private void putUser(UUID authId, Map<String, ?> body, boolean emailUpdate) {
		try {
			restClient.put()
					.uri("/auth/v1/admin/users/{id}", authId)
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			int status = ex.getStatusCode().value();
			String response = ex.getResponseBodyAsString();
			if (emailUpdate && (status == 409 || status == 422
					|| (response != null && response.toLowerCase().contains("already")))) {
				throw new DuplicateUserException("Email already exists");
			}
			log.warn("Auth update failed with status {}", status);
			throw new IllegalStateException(emailUpdate ? "Could not update auth email" : "Could not update password");
		}
	}

	private record AuthUser(String id) {
	}

	private record AuthSession(
			@com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken,
			@com.fasterxml.jackson.annotation.JsonProperty("refresh_token") String refreshToken,
			@com.fasterxml.jackson.annotation.JsonProperty("expires_in") long expiresIn,
			@com.fasterxml.jackson.annotation.JsonProperty("token_type") String tokenType) {
	}
}
