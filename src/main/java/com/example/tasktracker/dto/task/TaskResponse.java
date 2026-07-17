package com.example.tasktracker.dto.task;

import com.example.tasktracker.dto.user.UserResponse;
import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
		Long id,
		String title,
		String description,
		TaskStatus status,
		LocalDate dueDate,
		Priority priority,
		Long projectId,
		String projectName,
		UserResponse assignedUser,
		LocalDateTime createDate,
		LocalDateTime updateDate
) {
}
