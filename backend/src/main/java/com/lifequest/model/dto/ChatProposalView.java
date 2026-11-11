package com.lifequest.model.dto;

public record ChatProposalView(
		Long id,
		String kind,
		String title,
		String whenLabel,
		String why,
		String status) {
}
