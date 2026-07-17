package com.example.tasktracker.dto.task;

import jakarta.validation.constraints.NotNull;

public record TaskAssignmentRequest(
		@NotNull
		Long assignedUserId
) {
}
