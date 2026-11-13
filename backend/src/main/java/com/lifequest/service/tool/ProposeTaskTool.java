package com.lifequest.service.tool;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.lifequest.model.dto.TaskRequestDto;
import com.lifequest.model.entity.ActivityType;

import tools.jackson.databind.json.JsonMapper;

@Component
@Order(1)
public class ProposeTaskTool implements ChatTool {

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	private static final Duration PAST_START_GRACE = Duration.ofMinutes(15);
	private static final Duration MIN_DURATION = Duration.ofMinutes(15);
	private static final int MAX_TITLE = 80;
	private static final int MAX_WHY = 300;
	private static final String DEFAULT_COLOR = "purple";
	private static final String DEFAULT_ICON = "📆";
	private static final Set<String> COLORS = Set.of("purple", "red", "blue", "yellow", "green");
	private static final Set<String> PRIORITIES = Set.of("low", "medium", "high");

	private final JsonMapper jsonMapper;
	private final Clock clock;

	public ProposeTaskTool(JsonMapper jsonMapper, Clock clock) {
		this.jsonMapper = jsonMapper;
		this.clock = clock;
	}

	@Override
	public String name() {
		return "propose_task";
	}

	@Override
	public Map<String, Object> spec() {
		return Map.of(
				"type", "function",
				"function", Map.of(
						"name", name(),
						"description", "Propose a task (misión) for this user. Does not create it. The user must press Crear. Use the exact title they asked for. Call this once. If start is already in the past, the proposal is still stored and the start moves to a later time today or the next day.",
						"parameters", Map.of(
								"type", "object",
								"properties", Map.of(
										"title", Map.of("type", "string", "description", "Task title, 1 to 80 characters."),
										"date", Map.of("type", "string", "description", "Day yyyy-MM-dd in Europe/Madrid."),
										"start", Map.of("type", "string", "description", "Start time HH:mm."),
										"end", Map.of("type", "string", "description", "End time HH:mm, at least 15 minutes later, same UTC day."),
										"priority", Map.of("type", "string", "enum", List.of("low", "medium", "high")),
										"activityType", Map.of("type", "string", "enum", List.of("EXERCISE", "STUDY", "WORK", "SOCIAL", "SPORTS", "ENTERTAINMENT", "UNDEFINED")),
										"why", Map.of("type", "string", "description", "One sentence for the proposal card."),
										"color", Map.of("type", "string", "enum", List.of("purple", "red", "blue", "yellow", "green"), "description", "Defaults to purple when omitted."),
										"icon", Map.of("type", "string", "description", "Defaults to 📆 when omitted."),
										"description", Map.of("type", "string", "description", "Defaults to empty when omitted.")),
								"required", List.of("title", "date", "start", "end", "priority", "activityType", "why"))));
	}

	@Override
	public Outcome call(Long userId, String arguments) {
		ToolArguments args = ToolArguments.parse(jsonMapper, arguments);
		if (args.error() != null) {
			return Outcome.error(args.error());
		}
		String title = args.text("title");
		if (title.isEmpty() || title.length() > MAX_TITLE) {
			return Outcome.error("title must be 1 to " + MAX_TITLE + " characters");
		}
		String why = args.text("why");
		if (why.isEmpty() || why.length() > MAX_WHY) {
			return Outcome.error("why must be one sentence of at most " + MAX_WHY + " characters");
		}
		String priority = args.text("priority").toLowerCase();
		if (!PRIORITIES.contains(priority)) {
			return Outcome.error("priority must be low, medium or high");
		}
		ActivityType activityType = activityType(args.text("activityType"));
		if (activityType == null) {
			return Outcome.error("activityType must be one of EXERCISE, STUDY, WORK, SOCIAL, SPORTS, ENTERTAINMENT, UNDEFINED");
		}
		LocalDate date = date(args.text("date"));
		if (date == null) {
			return Outcome.error("date must be yyyy-MM-dd");
		}
		LocalTime startTime = time(args.text("start", "startTime"));
		LocalTime endTime = time(args.text("end", "endTime"));
		if (startTime == null || endTime == null) {
			return Outcome.error("start and end must be HH:mm");
		}
		Slot slot = resolve(date, startTime, endTime);
		if (slot.error() != null) {
			return Outcome.error(slot.error());
		}
		String color = args.text("color");
		if (color.isEmpty()) {
			color = DEFAULT_COLOR;
		} else if (!COLORS.contains(color)) {
			return Outcome.error("color must be one of " + COLORS);
		}
		String icon = args.text("icon");
		if (icon.isEmpty()) {
			icon = DEFAULT_ICON;
		}
		String description = args.text("description");
		TaskRequestDto request = new TaskRequestDto(
				title, description, icon, color, slot.date(), slot.start(), slot.end(), priority, activityType);
		String when = slot.date() + " " + slot.start().format(TIME) + "–" + slot.end().format(TIME);
		String message = "Pending proposal. It is not created until the user presses Crear.";
		if (slot.moved()) {
			message += " The requested start had already passed, so this proposal uses " + when
					+ ". Do not call propose_task again for this task.";
		}
		return new Outcome(message, new Draft("task", title, when, why, jsonMapper.writeValueAsString(request)));
	}

	private static ActivityType activityType(String value) {
		if (value.isEmpty()) {
			return null;
		}
		try {
			return ActivityType.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static LocalDate date(String value) {
		if (value.isEmpty()) {
			return null;
		}
		try {
			return LocalDate.parse(value);
		} catch (DateTimeParseException ex) {
			return null;
		}
	}

	private static LocalTime time(String value) {
		if (value.isEmpty()) {
			return null;
		}
		try {
			return LocalTime.parse(value);
		} catch (DateTimeParseException ex) {
			return null;
		}
	}

	// A start that already passed still becomes one pending card, later today or the next day.
	private Slot resolve(LocalDate date, LocalTime startTime, LocalTime endTime) {
		if (!endTime.isAfter(startTime)) {
			return Slot.bad("end must be after start");
		}
		Duration duration = Duration.between(startTime, endTime);
		if (duration.compareTo(MIN_DURATION) < 0) {
			return Slot.bad("Task must last at least " + MIN_DURATION.toMinutes() + " minutes");
		}
		if (!startsBeforeGrace(date, startTime)) {
			String utc = utcDay(date, startTime, endTime);
			return utc == null ? Slot.kept(date, startTime, endTime) : Slot.bad(utc);
		}
		Slot laterToday = laterToday(duration);
		if (laterToday != null) {
			return laterToday;
		}
		LocalDate nextDay = LocalDate.now(clock).plusDays(1);
		String utc = utcDay(nextDay, startTime, endTime);
		return utc == null ? Slot.moved(nextDay, startTime, endTime) : Slot.bad(utc);
	}

	private Slot laterToday(Duration duration) {
		ZonedDateTime now = clock.instant().atZone(clock.getZone());
		ZonedDateTime start = nextQuarter(now);
		if (!start.toLocalDate().equals(now.toLocalDate()) || start.toInstant().isBefore(clock.instant())) {
			return null;
		}
		ZonedDateTime end = start.plus(duration);
		if (!end.toLocalDate().equals(start.toLocalDate()) || !end.toLocalTime().isAfter(start.toLocalTime())) {
			return null;
		}
		if (utcDay(start.toLocalDate(), start.toLocalTime(), end.toLocalTime()) != null) {
			return null;
		}
		return Slot.moved(start.toLocalDate(), start.toLocalTime(), end.toLocalTime());
	}

	private static ZonedDateTime nextQuarter(ZonedDateTime now) {
		ZonedDateTime trimmed = now.withSecond(0).withNano(0);
		int remainder = trimmed.getMinute() % 15;
		ZonedDateTime boundary = remainder == 0 ? trimmed : trimmed.plusMinutes(15 - remainder);
		if (!boundary.isAfter(now)) {
			boundary = boundary.plusMinutes(15);
		}
		return boundary;
	}

	private boolean startsBeforeGrace(LocalDate date, LocalTime startTime) {
		Instant start = date.atTime(startTime).atZone(clock.getZone()).toInstant();
		return start.isBefore(clock.instant().minus(PAST_START_GRACE));
	}

	private String utcDay(LocalDate date, LocalTime startTime, LocalTime endTime) {
		Instant start = date.atTime(startTime).atZone(clock.getZone()).toInstant();
		Instant end = date.atTime(endTime).atZone(clock.getZone()).toInstant();
		if (start.atOffset(ZoneOffset.UTC).getDayOfMonth() != end.atOffset(ZoneOffset.UTC).getDayOfMonth()) {
			return "Task cannot cross midnight UTC";
		}
		return null;
	}

	private record Slot(LocalDate date, LocalTime start, LocalTime end, boolean moved, String error) {

		static Slot bad(String error) {
			return new Slot(null, null, null, false, error);
		}

		static Slot kept(LocalDate date, LocalTime start, LocalTime end) {
			return new Slot(date, start, end, false, null);
		}

		static Slot moved(LocalDate date, LocalTime start, LocalTime end) {
			return new Slot(date, start, end, true, null);
		}
	}
}
