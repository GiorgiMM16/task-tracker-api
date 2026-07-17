package com.example.tasktracker.service;

import com.example.tasktracker.dto.project.ProjectRequest;
import com.example.tasktracker.dto.project.ProjectResponse;
import com.example.tasktracker.entity.Project;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.exception.BadRequestException;
import com.example.tasktracker.exception.ResourceNotFoundException;
import com.example.tasktracker.mapper.ProjectMapper;
import com.example.tasktracker.model.Role;
import com.example.tasktracker.repository.ProjectRepository;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.security.CurrentUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;
	private final ProjectMapper projectMapper;

	@Transactional(readOnly = true)
	public List<ProjectResponse> getProjects() {
		User currentUser = currentUserService.getCurrentUser();
		if (currentUser.getRole() == Role.ADMIN) {
			return projectRepository.findAll().stream()
					.map(projectMapper::toResponse)
					.toList();
		}
		if (currentUser.getRole() == Role.MANAGER) {
			return projectRepository.findByOwnerId(currentUser.getId()).stream()
					.map(projectMapper::toResponse)
					.toList();
		}
		throw new AccessDeniedException("Only admins and managers can view projects");
	}

	@Transactional(readOnly = true)
	public ProjectResponse getProject(Long id) {
		Project project = getProjectForManagement(id);
		return projectMapper.toResponse(project);
	}

	@Transactional
	public ProjectResponse createProject(ProjectRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		requireAdminOrManager(currentUser);

		User owner = resolveOwner(request.ownerId(), currentUser);
		Project project = Project.builder()
				.name(request.name())
				.description(request.description())
				.owner(owner)
				.build();

		return projectMapper.toResponse(projectRepository.save(project));
	}

	@Transactional
	public ProjectResponse updateProject(Long id, ProjectRequest request) {
		Project project = getProjectForManagement(id);
		User currentUser = currentUserService.getCurrentUser();

		project.setName(request.name());
		project.setDescription(request.description());
		if (request.ownerId() != null) {
			if (currentUser.getRole() != Role.ADMIN) {
				throw new AccessDeniedException("Only admins can change project ownership");
			}
			project.setOwner(resolveOwner(request.ownerId(), currentUser));
		}

		return projectMapper.toResponse(project);
	}

	@Transactional
	public void deleteProject(Long id) {
		Project project = getProjectForManagement(id);
		projectRepository.delete(project);
	}

	private Project getProjectForManagement(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		if (currentUser.getRole() == Role.ADMIN || ownsProject(currentUser, project)) {
			return project;
		}
		throw new AccessDeniedException("You do not have access to this project");
	}

	private User resolveOwner(Long ownerId, User currentUser) {
		if (ownerId == null) {
			return currentUser;
		}
		if (currentUser.getRole() != Role.ADMIN && !ownerId.equals(currentUser.getId())) {
			throw new AccessDeniedException("Managers can only create projects for themselves");
		}

		User owner = userRepository.findById(ownerId)
				.orElseThrow(() -> new ResourceNotFoundException("Project owner not found"));
		if (owner.getRole() == Role.USER) {
			throw new BadRequestException("Project owner must be an admin or manager");
		}
		return owner;
	}

	private void requireAdminOrManager(User user) {
		if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
			throw new AccessDeniedException("Only admins and managers can manage projects");
		}
	}

	private boolean ownsProject(User user, Project project) {
		return user.getRole() == Role.MANAGER && project.getOwner().getId().equals(user.getId());
	}
}
