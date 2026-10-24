package com.lifequest.exception;

// The request is valid but the task is not in a state that allows it (already completed, window closed...).
public class InvalidStateException extends RuntimeException {

	public InvalidStateException(String message) {
		super(message);
	}
}
