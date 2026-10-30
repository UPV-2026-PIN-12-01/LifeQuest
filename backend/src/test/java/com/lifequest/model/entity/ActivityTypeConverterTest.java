package com.lifequest.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityTypeConverterTest {

	private final ActivityTypeConverter converter = new ActivityTypeConverter();

	@Test
	void convertsBackendActivityTypesToExistingPostgresEnumLabels() {
		assertThat(converter.convertToDatabaseColumn(ActivityType.EXERCISE)).isEqualTo("physical");
		assertThat(converter.convertToDatabaseColumn(ActivityType.STUDY)).isEqualTo("study");
		assertThat(converter.convertToDatabaseColumn(ActivityType.WORK)).isEqualTo("work");
		assertThat(converter.convertToDatabaseColumn(ActivityType.SOCIAL)).isEqualTo("social");
		assertThat(converter.convertToDatabaseColumn(ActivityType.SPORTS)).isEqualTo("sport");
		assertThat(converter.convertToDatabaseColumn(ActivityType.ENTERTAINMENT)).isEqualTo("leisure");
		assertThat(converter.convertToDatabaseColumn(ActivityType.UNDEFINED)).isEqualTo("undefined");
	}

	@Test
	void convertsExistingPostgresEnumLabelsToBackendActivityTypes() {
		assertThat(converter.convertToEntityAttribute("physical")).isEqualTo(ActivityType.EXERCISE);
		assertThat(converter.convertToEntityAttribute("sport")).isEqualTo(ActivityType.SPORTS);
		assertThat(converter.convertToEntityAttribute("leisure")).isEqualTo(ActivityType.ENTERTAINMENT);
		assertThat(converter.convertToEntityAttribute("undefined")).isEqualTo(ActivityType.UNDEFINED);
	}
}
