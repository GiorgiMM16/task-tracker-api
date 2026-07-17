package com.example.tasktracker.dto.task;

import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskCreateRequest(
		@NotBlank
		@Size(max = 255)
		String title,

		@Size(max = 2000)
		String description,

		TaskStatus status,

		LocalDate dueDate,

		Priority priority,

		@NotNull
		Long projectId,

		Long assignedUserId
) {
}
