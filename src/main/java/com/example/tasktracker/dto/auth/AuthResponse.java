package com.example.tasktracker.dto.auth;

import com.example.tasktracker.dto.user.UserResponse;

public record AuthResponse(
		String token,
		UserResponse user
) {
}
