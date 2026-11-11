package com.lifequest.service.tool;

import java.util.Map;

// One Groq function. A new action is a new bean. The call does not insert a task or a challenge.
public interface ChatTool {

	String name();

	Map<String, Object> spec();

	Outcome call(Long userId, String arguments);

	record Draft(String kind, String title, String whenLabel, String why, String payload) {
	}

	record Outcome(String message, Draft draft) {

		static Outcome error(String message) {
			return new Outcome(message, null);
		}

		static Outcome pending(Draft draft) {
			return new Outcome("Pending proposal. It is not created until the user presses Crear.", draft);
		}
	}
}
