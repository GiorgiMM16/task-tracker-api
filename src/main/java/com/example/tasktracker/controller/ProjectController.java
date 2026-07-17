package com.example.tasktracker.controller;

import com.example.tasktracker.dto.project.ProjectRequest;
import com.example.tasktracker.dto.project.ProjectResponse;
import com.example.tasktracker.service.ProjectService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ProjectController {

	private final ProjectService projectService;

	@GetMapping
	public List<ProjectResponse> getProjects() {
		return projectService.getProjects();
	}

	@GetMapping("/{id}")
	public ProjectResponse getProject(@PathVariable Long id) {
		return projectService.getProject(id);
	}

	@PostMapping
	public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
		ProjectResponse project = projectService.createProject(request);
		return ResponseEntity.created(URI.create("/api/projects/" + project.id())).body(project);
	}

	@PutMapping("/{id}")
	public ProjectResponse updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
		return projectService.updateProject(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
		projectService.deleteProject(id);
		return ResponseEntity.noContent().build();
	}
}
