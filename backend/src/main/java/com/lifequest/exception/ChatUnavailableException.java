package com.lifequest.exception;

public class ChatUnavailableException extends RuntimeException {

	public ChatUnavailableException() {
		super("Chat is unavailable");
	}
}
