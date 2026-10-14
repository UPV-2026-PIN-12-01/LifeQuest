package com.lifequest.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.lifequest.config.SupabaseProperties;

@Service
public class SupabaseStorageService {

	private static final String BUCKET = "avatars";

	private final RestClient restClient;
	private final String publicBaseUrl;

	public SupabaseStorageService(
			@Qualifier("supabaseRestClient") RestClient restClient,
			SupabaseProperties properties) {
		this.restClient = restClient;
		String url = properties.url() == null ? "" : properties.url().replaceAll("/+$", "");
		this.publicBaseUrl = url;
	}

	public String uploadAvatar(UUID innerId, byte[] bytes, String contentType, String objectName) {
		restClient.post()
				.uri("/storage/v1/object/{bucket}/{innerId}/{objectName}", BUCKET, innerId, objectName)
				.contentType(MediaType.parseMediaType(contentType))
				.header("x-upsert", "true")
				.body(bytes)
				.retrieve()
				.toBodilessEntity();
		return publicBaseUrl + "/storage/v1/object/public/" + BUCKET + "/" + innerId + "/" + objectName;
	}
}
