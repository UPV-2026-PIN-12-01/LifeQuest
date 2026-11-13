package com.lifequest.model.dto;

import java.util.List;

public record ChatThreadDetail(Long id, String title, List<ChatLine> messages) {
}
