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
				throw new DuplicateUserException("Username or email already exists");
			}
			log.warn("Auth create failed with status {}", status);
			throw new IllegalStateException("Could not create auth user");
		}
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

	private record AuthUser(String id) {
	}
}
