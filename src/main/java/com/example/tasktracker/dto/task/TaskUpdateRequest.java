package com.example.tasktracker.dto.task;

import com.example.tasktracker.model.Priority;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskUpdateRequest(
		@Size(max = 255)
		String title,

		@Size(max = 2000)
		String description,

		LocalDate dueDate,

		Priority priority,

		Long projectId,

		Long assignedUserId
) {
}
