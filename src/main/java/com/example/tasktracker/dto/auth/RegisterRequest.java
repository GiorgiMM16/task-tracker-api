package com.example.tasktracker.dto.auth;

import com.example.tasktracker.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank
		@Email
		String email,

		@NotBlank
		@Size(min = 6, max = 100)
		String password,

		Role role
) {
}
