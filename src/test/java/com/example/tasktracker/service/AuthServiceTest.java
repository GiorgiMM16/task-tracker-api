package com.example.tasktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tasktracker.dto.auth.AuthResponse;
import com.example.tasktracker.dto.auth.LoginRequest;
import com.example.tasktracker.dto.auth.RegisterRequest;
import com.example.tasktracker.dto.user.UserResponse;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.exception.DuplicateResourceException;
import com.example.tasktracker.mapper.UserMapper;
import com.example.tasktracker.model.Role;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private UserMapper userMapper;

	private PasswordEncoder passwordEncoder;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BCryptPasswordEncoder();
		authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService, userMapper);
	}

	@Test
	void registerHashesPasswordAndDefaultsRoleToUser() {
		RegisterRequest request = new RegisterRequest("New.User@Example.com", "password123", null);
		UserResponse userResponse = new UserResponse(1L, "new.user@example.com", Role.USER, null, null);

		when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(1L);
			return user;
		});
		when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
		when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

		AuthResponse response = authService.register(request);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.getEmail()).isEqualTo("new.user@example.com");
		assertThat(savedUser.getRole()).isEqualTo(Role.USER);
		assertThat(savedUser.getPassword()).isNotEqualTo("password123");
		assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.user()).isEqualTo(userResponse);
	}

	@Test
	void registerRejectsDuplicateEmail() {
		RegisterRequest request = new RegisterRequest("user@example.com", "password123", Role.USER);
		when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateResourceException.class)
				.hasMessage("A user with this email already exists");
	}

	@Test
	void loginAuthenticatesAndReturnsToken() {
		LoginRequest request = new LoginRequest("MANAGER@example.com", "password123");
		User user = User.builder()
				.id(2L)
				.email("manager@example.com")
				.password("hashed")
				.role(Role.MANAGER)
				.build();
		UserResponse userResponse = new UserResponse(2L, "manager@example.com", Role.MANAGER, null, null);

		when(userRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(user));
		when(jwtService.generateToken(user)).thenReturn("manager-token");
		when(userMapper.toResponse(user)).thenReturn(userResponse);

		AuthResponse response = authService.login(request);

		ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
				ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
		verify(authenticationManager).authenticate(tokenCaptor.capture());

		assertThat(tokenCaptor.getValue().getPrincipal()).isEqualTo("manager@example.com");
		assertThat(tokenCaptor.getValue().getCredentials()).isEqualTo("password123");
		assertThat(response.token()).isEqualTo("manager-token");
		assertThat(response.user()).isEqualTo(userResponse);
	}
}
