package com.example.tasktracker.dto.task;

import com.example.tasktracker.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
		@NotNull
		TaskStatus status
) {
}
