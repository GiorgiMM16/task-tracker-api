package com.example.tasktracker.dto.project;

import com.example.tasktracker.dto.user.UserResponse;
import java.time.LocalDateTime;

public record ProjectResponse(
		Long id,
		String name,
		String description,
		UserResponse owner,
		LocalDateTime createDate,
		LocalDateTime updateDate
) {
}
