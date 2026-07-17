package com.example.tasktracker.service;

import com.example.tasktracker.dto.task.TaskAssignmentRequest;
import com.example.tasktracker.dto.task.TaskCreateRequest;
import com.example.tasktracker.dto.task.TaskResponse;
import com.example.tasktracker.dto.task.TaskStatusUpdateRequest;
import com.example.tasktracker.dto.task.TaskUpdateRequest;
import com.example.tasktracker.entity.Project;
import com.example.tasktracker.entity.Task;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.exception.ResourceNotFoundException;
import com.example.tasktracker.mapper.TaskMapper;
import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.Role;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.repository.ProjectRepository;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.security.CurrentUserService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;
	private final TaskMapper taskMapper;

	@Transactional(readOnly = true)
	public TaskResponse getTask(Long id) {
		Task task = findTask(id);
		requireTaskReadAccess(task, currentUserService.getCurrentUser());
		return taskMapper.toResponse(task);
	}

	@Transactional
	public TaskResponse createTask(TaskCreateRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Project project = findProject(request.projectId());
		requireProjectTaskManagement(project, currentUser);

		Task task = Task.builder()
				.title(request.title())
				.description(request.description())
				.status(request.status() == null ? TaskStatus.TODO : request.status())
				.dueDate(request.dueDate())
				.priority(request.priority() == null ? Priority.MEDIUM : request.priority())
				.project(project)
				.assignedUser(resolveAssignedUser(request.assignedUserId()))
				.build();

		return taskMapper.toResponse(taskRepository.save(task));
	}

	@Transactional
	public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = findTask(id);
		requireProjectTaskManagement(task.getProject(), currentUser);

		if (request.title() != null) {
			task.setTitle(request.title());
		}
		if (request.description() != null) {
			task.setDescription(request.description());
		}
		if (request.dueDate() != null) {
			task.setDueDate(request.dueDate());
		}
		if (request.priority() != null) {
			task.setPriority(request.priority());
		}
		if (request.projectId() != null && !request.projectId().equals(task.getProject().getId())) {
			Project newProject = findProject(request.projectId());
			requireProjectTaskManagement(newProject, currentUser);
			task.setProject(newProject);
		}
		if (request.assignedUserId() != null) {
			task.setAssignedUser(resolveAssignedUser(request.assignedUserId()));
		}

		return taskMapper.toResponse(task);
	}

	@Transactional
	public TaskResponse assignTask(Long id, TaskAssignmentRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = findTask(id);
		requireProjectTaskManagement(task.getProject(), currentUser);
		task.setAssignedUser(resolveAssignedUser(request.assignedUserId()));
		return taskMapper.toResponse(task);
	}

	@Transactional
	public TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = findTask(id);
		if (currentUser.getRole() != Role.ADMIN && !isAssignedTo(currentUser, task)) {
			throw new AccessDeniedException("Only the assigned user can update task status");
		}
		task.setStatus(request.status());
		return taskMapper.toResponse(task);
	}

	@Transactional
	public void deleteTask(Long id) {
		User currentUser = currentUserService.getCurrentUser();
		Task task = findTask(id);
		requireProjectTaskManagement(task.getProject(), currentUser);
		taskRepository.delete(task);
	}

	@Transactional(readOnly = true)
	public Page<TaskResponse> getTasks(
			Long projectId,
			Long assignedUserId,
			TaskStatus status,
			Priority priority,
			Pageable pageable
	) {
		User currentUser = currentUserService.getCurrentUser();
		validateRequestedFilters(projectId, assignedUserId, currentUser);

		Specification<Task> specification = buildSpecification(projectId, assignedUserId, status, priority, currentUser);
		return taskRepository.findAll(specification, pageable).map(taskMapper::toResponse);
	}

	private Specification<Task> buildSpecification(
			Long projectId,
			Long assignedUserId,
			TaskStatus status,
			Priority priority,
			User currentUser
	) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (projectId != null) {
				predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
			}
			if (assignedUserId != null) {
				predicates.add(criteriaBuilder.equal(root.get("assignedUser").get("id"), assignedUserId));
			}
			if (status != null) {
				predicates.add(criteriaBuilder.equal(root.get("status"), status));
			}
			if (priority != null) {
				predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
			}

			if (currentUser.getRole() == Role.MANAGER) {
				predicates.add(criteriaBuilder.equal(root.get("project").get("owner").get("id"), currentUser.getId()));
			} else if (currentUser.getRole() == Role.USER) {
				predicates.add(criteriaBuilder.equal(root.get("assignedUser").get("id"), currentUser.getId()));
			}

			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private void validateRequestedFilters(Long projectId, Long assignedUserId, User currentUser) {
		if (projectId != null) {
			Project project = findProject(projectId);
			if (!canManageProjectTasks(project, currentUser) && currentUser.getRole() != Role.USER) {
				throw new AccessDeniedException("You do not have access to this project");
			}
		}

		if (currentUser.getRole() == Role.USER && assignedUserId != null && !assignedUserId.equals(currentUser.getId())) {
			throw new AccessDeniedException("Users can only view their own assigned tasks");
		}
	}

	private void requireTaskReadAccess(Task task, User currentUser) {
		if (canManageProjectTasks(task.getProject(), currentUser) || isAssignedTo(currentUser, task)) {
			return;
		}
		throw new AccessDeniedException("You do not have access to this task");
	}

	private void requireProjectTaskManagement(Project project, User currentUser) {
		if (!canManageProjectTasks(project, currentUser)) {
			throw new AccessDeniedException("Only admins or the project owner can manage tasks for this project");
		}
	}

	private boolean canManageProjectTasks(Project project, User currentUser) {
		return currentUser.getRole() == Role.ADMIN
				|| (currentUser.getRole() == Role.MANAGER && project.getOwner().getId().equals(currentUser.getId()));
	}

	private boolean isAssignedTo(User user, Task task) {
		return task.getAssignedUser() != null && task.getAssignedUser().getId().equals(user.getId());
	}

	private Task findTask(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));
	}

	private Project findProject(Long id) {
		return projectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
	}

	private User resolveAssignedUser(Long assignedUserId) {
		if (assignedUserId == null) {
			return null;
		}
		return userRepository.findById(assignedUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
	}
}
