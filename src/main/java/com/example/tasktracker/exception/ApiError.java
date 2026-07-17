package com.example.tasktracker.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> validationErrors
) {

	public static ApiError of(int status, String error, String message, String path) {
		return new ApiError(LocalDateTime.now(), status, error, message, path, null);
	}

	public static ApiError withValidationErrors(
			int status,
			String error,
			String message,
			String path,
			Map<String, String> validationErrors
	) {
		return new ApiError(LocalDateTime.now(), status, error, message, path, validationErrors);
	}
}
