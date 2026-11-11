package com.lifequest.model.dto;

import java.time.Instant;

public record ChatThreadSummary(Long id, String title, String preview, Instant updatedAt) {
}
