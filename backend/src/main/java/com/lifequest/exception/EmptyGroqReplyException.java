package com.lifequest.exception;

// Groq finished a round with no text and no tool call. Same 502 as ChatUnavailableException when nothing was proposed.
public class EmptyGroqReplyException extends ChatUnavailableException {
}
