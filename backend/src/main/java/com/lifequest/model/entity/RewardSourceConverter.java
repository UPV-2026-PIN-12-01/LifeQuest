package com.lifequest.model.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RewardSourceConverter implements AttributeConverter<RewardSource, String> {

	@Override
	public String convertToDatabaseColumn(RewardSource source) {
		return source == null ? null : source.databaseValue();
	}

	@Override
	public RewardSource convertToEntityAttribute(String value) {
		return value == null ? null : RewardSource.fromDatabaseValue(value);
	}
}
