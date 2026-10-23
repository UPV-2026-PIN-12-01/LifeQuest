package com.lifequest.service;

import java.time.Duration;
import java.time.Instant;

import com.lifequest.model.entity.Task;
import com.lifequest.model.entity.TaskSession;

// How much the server can vouch for a task, from its session alone. The client never sends durations.
public enum TaskVerification {

	// Only the user's word: base XP.
	NONE("none", 1.0),
	// Checked in and out around the planned time, without the page open (sport, going out...).
	CHECKIN("checkin", 1.2),
	// The page stayed open and visible, measured by heartbeats (studying at the computer).
	FOCUS("focus", 1.5);

	// Share of the planned duration a session must cover to count.
	static final double REQUIRED_SHARE = 0.7;
	// Check-out later than this after the planned end does not count as a check-in.
	static final Duration CHECKOUT_GRACE = Duration.ofMinutes(30);
	// A longer gap between heartbeats means the page was hidden or closed, so that time is not focus.
	static final Duration MAX_HEARTBEAT_GAP = Duration.ofSeconds(90);

	private final String id;
	private final double multiplier;

	TaskVerification(String id, double multiplier) {
		this.id = id;
		this.multiplier = multiplier;
	}

	public String id() {
		return id;
	}

	public long apply(long baseXp) {
		return Math.round(baseXp * multiplier);
	}

	// An open session is judged as if it were checked out now.
	static TaskVerification of(Task task, TaskSession session, Instant now) {
		if (session == null) {
			return NONE;
		}
		long required = (long) (Duration.between(task.getStartTime(), task.getEndTime()).toSeconds() * REQUIRED_SHARE);
		if (session.getFocusedSeconds() >= required) {
			return FOCUS;
		}
		Instant checkout = session.isOpen() ? now : session.getEndedAt();
		if (checkout.isAfter(task.getEndTime().plus(CHECKOUT_GRACE))) {
			return NONE;
		}
		return secondsWithin(task, session.getStartedAt(), checkout) >= required ? CHECKIN : NONE;
	}

	// Adds the time since the last heartbeat to the focus, if the gap was short; only time inside the task counts.
	static void recordHeartbeat(Task task, TaskSession session, Instant now) {
		Instant last = session.getLastHeartbeatAt();
		if (!now.isAfter(last)) {
			return;
		}
		if (Duration.between(last, now).compareTo(MAX_HEARTBEAT_GAP) <= 0) {
			session.setFocusedSeconds(session.getFocusedSeconds() + secondsWithin(task, last, now));
		}
		session.setLastHeartbeatAt(now);
	}

	private static long secondsWithin(Task task, Instant from, Instant to) {
		Instant start = from.isAfter(task.getStartTime()) ? from : task.getStartTime();
		Instant end = to.isBefore(task.getEndTime()) ? to : task.getEndTime();
		return end.isAfter(start) ? Duration.between(start, end).toSeconds() : 0;
	}
}
