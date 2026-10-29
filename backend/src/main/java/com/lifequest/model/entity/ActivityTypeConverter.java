package com.lifequest.model.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ActivityTypeConverter implements AttributeConverter<ActivityType, String> {

	@Override
	public String convertToDatabaseColumn(ActivityType activityType) {
		if (activityType == null) {
			return null;
		}
		return switch (activityType) {
			case EXERCISE -> "physical";
			case STUDY -> "study";
			case WORK -> "work";
			case SOCIAL -> "social";
			case SPORTS -> "sport";
			case ENTERTAINMENT -> "leisure";
			case UNDEFINED -> "undefined";
		};
	}

	@Override
	public ActivityType convertToEntityAttribute(String value) {
		if (value == null) {
			return null;
		}
		return switch (value.toLowerCase()) {
			case "exercise", "physical" -> ActivityType.EXERCISE;
			case "study" -> ActivityType.STUDY;
			case "work" -> ActivityType.WORK;
			case "social" -> ActivityType.SOCIAL;
			case "sports", "sport" -> ActivityType.SPORTS;
			case "entertainment", "leisure" -> ActivityType.ENTERTAINMENT;
			case "undefined" -> ActivityType.UNDEFINED;
			default -> throw new IllegalArgumentException("Unknown activity type stored in database: " + value);
		};
	}
}
