package com.lifequest.model.dto;

import jakarta.validation.constraints.NotBlank;

public record MarketPurchaseRequest(@NotBlank String catalogId) {
}
