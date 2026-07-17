package com.example.tasktracker.security;

import com.example.tasktracker.model.Role;

public record JwtClaims(
		Long userId,
		String email,
		Role role
) {
}
