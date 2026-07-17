package com.example.tasktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tasktracker.dto.project.ProjectRequest;
import com.example.tasktracker.dto.project.ProjectResponse;
import com.example.tasktracker.dto.user.UserResponse;
import com.example.tasktracker.entity.Project;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.mapper.ProjectMapper;
import com.example.tasktracker.model.Role;
import com.example.tasktracker.repository.ProjectRepository;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.security.CurrentUserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private ProjectMapper projectMapper;

	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		projectService = new ProjectService(projectRepository, userRepository, currentUserService, projectMapper);
	}

	@Test
	void managerCannotReadAnotherManagersProject() {
		User currentManager = user(1L, Role.MANAGER);
		User otherManager = user(2L, Role.MANAGER);
		Project project = Project.builder()
				.id(10L)
				.name("Other project")
				.owner(otherManager)
				.build();

		when(currentUserService.getCurrentUser()).thenReturn(currentManager);
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		assertThatThrownBy(() -> projectService.getProject(10L))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("You do not have access to this project");
	}

	@Test
	void adminCanCreateProjectForManagerOwner() {
		User admin = user(1L, Role.ADMIN);
		User manager = user(2L, Role.MANAGER);
		ProjectRequest request = new ProjectRequest("Platform", "Internal work", 2L);
		ProjectResponse response = new ProjectResponse(
				99L,
				"Platform",
				"Internal work",
				new UserResponse(2L, "user2@example.com", Role.MANAGER, null, null),
				null,
				null
		);

		when(currentUserService.getCurrentUser()).thenReturn(admin);
		when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
		when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
			Project project = invocation.getArgument(0);
			project.setId(99L);
			return project;
		});
		when(projectMapper.toResponse(any(Project.class))).thenReturn(response);

		ProjectResponse createdProject = projectService.createProject(request);

		ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
		verify(projectRepository).save(projectCaptor.capture());

		assertThat(projectCaptor.getValue().getOwner()).isEqualTo(manager);
		assertThat(projectCaptor.getValue().getName()).isEqualTo("Platform");
		assertThat(createdProject).isEqualTo(response);
	}

	private User user(Long id, Role role) {
		return User.builder()
				.id(id)
				.email("user" + id + "@example.com")
				.password("hashed")
				.role(role)
				.build();
	}
}
