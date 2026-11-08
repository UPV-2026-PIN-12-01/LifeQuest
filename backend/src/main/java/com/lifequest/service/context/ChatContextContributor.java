package com.lifequest.service.context;

import java.util.List;

// One block of the prompt. A new source is a new bean; it does not edit the Groq call.
public interface ChatContextContributor {

	String text(Long userId);

	static String list(String header, List<String> items) {
		if (items.isEmpty()) {
			return header + "\n- ninguna";
		}
		return header + "\n- " + String.join("\n- ", items);
	}
}
