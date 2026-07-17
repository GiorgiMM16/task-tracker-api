package com.example.tasktracker.dto.user;

import com.example.tasktracker.model.Role;
import java.time.LocalDateTime;

public record UserResponse(
		Long id,
		String email,
		Role role,
		LocalDateTime createDate,
		LocalDateTime updateDate
) {
}
