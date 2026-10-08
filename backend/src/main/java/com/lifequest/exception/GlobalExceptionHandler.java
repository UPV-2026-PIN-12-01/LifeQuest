package com.lifequest.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidSignupException.class)
	public ResponseEntity<Map<String, String>> invalidSignup(InvalidSignupException ex) {
		return error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, String>> missingParam(MissingServletRequestParameterException ex) {
		return error(HttpStatus.BAD_REQUEST, ex.getParameterName() + " is required");
	}

	@ExceptionHandler(DuplicateUserException.class)
	public ResponseEntity<Map<String, String>> duplicateUser(DuplicateUserException ex) {
		return error(HttpStatus.CONFLICT, ex.getMessage());
	}

	private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of("error", message));
	}
}
