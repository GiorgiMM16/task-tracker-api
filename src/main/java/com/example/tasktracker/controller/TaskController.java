package com.example.tasktracker.controller;

import com.example.tasktracker.dto.task.TaskAssignmentRequest;
import com.example.tasktracker.dto.task.TaskCreateRequest;
import com.example.tasktracker.dto.task.TaskResponse;
import com.example.tasktracker.dto.task.TaskStatusUpdateRequest;
import com.example.tasktracker.dto.task.TaskUpdateRequest;
import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

	private final TaskService taskService;

	@GetMapping
	public Page<TaskResponse> getTasks(
			@RequestParam(required = false) Long projectId,
			@RequestParam(required = false) Long assignedUserId,
			@RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) Priority priority,
			@PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable
	) {
		return taskService.getTasks(projectId, assignedUserId, status, priority, pageable);
	}

	@GetMapping("/{id}")
	public TaskResponse getTask(@PathVariable Long id) {
		return taskService.getTask(id);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
		TaskResponse task = taskService.createTask(request);
		return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
		return taskService.updateTask(id, request);
	}

	@PatchMapping("/{id}/assignee")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public TaskResponse assignTask(@PathVariable Long id, @Valid @RequestBody TaskAssignmentRequest request) {
		return taskService.assignTask(id, request);
	}

	@PatchMapping("/{id}/status")
	public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateRequest request) {
		return taskService.updateStatus(id, request);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
		return ResponseEntity.noContent().build();
	}
}
