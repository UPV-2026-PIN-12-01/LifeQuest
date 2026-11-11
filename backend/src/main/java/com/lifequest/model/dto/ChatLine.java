package com.lifequest.model.dto;

import java.util.List;

public record ChatLine(String role, String text, List<ChatProposalView> proposals) {
}
