package com.lifequest.service.context;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.lifequest.model.dto.TaskDto;
import com.lifequest.service.TaskService;

@Component
@Order(2)
public class WeekTasksChatContext implements ChatContextContributor {

	private final TaskService taskService;
	private final Clock clock;

	public WeekTasksChatContext(TaskService taskService, Clock clock) {
		this.taskService = taskService;
		this.clock = clock;
	}

	// Monday through Sunday in the app time zone (Europe/Madrid).
	@Override
	public String text(Long userId) {
		LocalDate today = LocalDate.now(clock);
		LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		List<String> lines = taskService.findForUser(userId, monday, sunday).stream()
				.map(WeekTasksChatContext::line)
				.toList();
		return ChatContextContributor.list(
				"Tareas de esta semana (" + monday + " a " + sunday + "):",
				lines);
	}

	private static String line(TaskDto task) {
		return "id " + task.id() + " " + task.date() + " " + task.startTime() + "–" + task.endTime() + " " + task.title()
				+ " (" + statusInSpanish(task.status()) + ")";
	}

	private static String statusInSpanish(String status) {
		return switch (status == null ? "" : status) {
			case "pending" -> "pendiente";
			case "in_progress" -> "en curso";
			case "finished" -> "finalizada";
			case "completed" -> "completada";
			case "not_completed" -> "no completada";
			default -> status == null ? "" : status;
		};
	}
}
