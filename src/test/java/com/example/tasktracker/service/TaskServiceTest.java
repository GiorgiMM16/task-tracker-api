package com.example.tasktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.tasktracker.dto.task.TaskCreateRequest;
import com.example.tasktracker.dto.task.TaskResponse;
import com.example.tasktracker.dto.task.TaskStatusUpdateRequest;
import com.example.tasktracker.entity.Project;
import com.example.tasktracker.entity.Task;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.mapper.TaskMapper;
import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.Role;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.repository.ProjectRepository;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.security.CurrentUserService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private TaskMapper taskMapper;

	private TaskService taskService;

	@BeforeEach
	void setUp() {
		taskService = new TaskService(taskRepository, projectRepository, userRepository, currentUserService, taskMapper);
	}

	@Test
	void managerCanCreateTaskInOwnedProject() {
		User manager = user(1L, Role.MANAGER);
		User assignedUser = user(3L, Role.USER);
		Project project = project(5L, manager);
		TaskCreateRequest request = new TaskCreateRequest(
				"Build API",
				"Implement requirements",
				null,
				LocalDate.of(2026, 8, 1),
				Priority.HIGH,
				5L,
				3L
		);
		TaskResponse response = new TaskResponse(8L, "Build API", "Implement requirements", TaskStatus.TODO,
				LocalDate.of(2026, 8, 1), Priority.HIGH, 5L, "Project", null, null, null);

		when(currentUserService.getCurrentUser()).thenReturn(manager);
		when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
		when(userRepository.findById(3L)).thenReturn(Optional.of(assignedUser));
		when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
			Task task = invocation.getArgument(0);
			task.setId(8L);
			return task;
		});
		when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

		TaskResponse createdTask = taskService.createTask(request);

		ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
		verify(taskRepository).save(taskCaptor.capture());

		assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
		assertThat(taskCaptor.getValue().getAssignedUser()).isEqualTo(assignedUser);
		assertThat(createdTask).isEqualTo(response);
	}

	@Test
	void assignedUserCanUpdateTaskStatus() {
		User manager = user(1L, Role.MANAGER);
		User assignedUser = user(3L, Role.USER);
		Task task = task(10L, project(5L, manager), assignedUser);
		TaskResponse response = new TaskResponse(10L, "Task", null, TaskStatus.IN_PROGRESS,
				null, Priority.MEDIUM, 5L, "Project", null, null, null);

		when(currentUserService.getCurrentUser()).thenReturn(assignedUser);
		when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
		when(taskMapper.toResponse(task)).thenReturn(response);

		TaskResponse updatedTask = taskService.updateStatus(10L, new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS));

		assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
		assertThat(updatedTask).isEqualTo(response);
	}

	@Test
	void projectOwnerCannotUpdateStatusUnlessAssigned() {
		User manager = user(1L, Role.MANAGER);
		User assignedUser = user(3L, Role.USER);
		Task task = task(10L, project(5L, manager), assignedUser);

		when(currentUserService.getCurrentUser()).thenReturn(manager);
		when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

		assertThatThrownBy(() -> taskService.updateStatus(10L, new TaskStatusUpdateRequest(TaskStatus.DONE)))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Only the assigned user can update task status");
	}

	@Test
	void userCannotListTasksForAnotherAssignedUser() {
		User user = user(3L, Role.USER);
		when(currentUserService.getCurrentUser()).thenReturn(user);

		assertThatThrownBy(() -> taskService.getTasks(null, 4L, null, null, PageRequest.of(0, 10)))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("Users can only view their own assigned tasks");

		verifyNoInteractions(taskRepository);
	}

	private User user(Long id, Role role) {
		return User.builder()
				.id(id)
				.email("user" + id + "@example.com")
				.password("hashed")
				.role(role)
				.build();
	}

	private Project project(Long id, User owner) {
		return Project.builder()
				.id(id)
				.name("Project")
				.owner(owner)
				.build();
	}

	private Task task(Long id, Project project, User assignedUser) {
		return Task.builder()
				.id(id)
				.title("Task")
				.status(TaskStatus.TODO)
				.priority(Priority.MEDIUM)
				.project(project)
				.assignedUser(assignedUser)
				.build();
	}
}
