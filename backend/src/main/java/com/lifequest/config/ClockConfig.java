package com.lifequest.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

	// The zone decides which calendar day a task falls on; tests swap in a fixed clock.
	@Bean
	Clock clock(@Value("${lifequest.time-zone:Europe/Madrid}") String timeZone) {
		return Clock.system(ZoneId.of(timeZone));
	}
}
