package com.example.tasktracker.service;

import com.example.tasktracker.dto.auth.AuthResponse;
import com.example.tasktracker.dto.auth.LoginRequest;
import com.example.tasktracker.dto.auth.RegisterRequest;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.exception.DuplicateResourceException;
import com.example.tasktracker.exception.ResourceNotFoundException;
import com.example.tasktracker.mapper.UserMapper;
import com.example.tasktracker.model.Role;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserMapper userMapper;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("A user with this email already exists");
		}

		User user = User.builder()
				.email(email)
				.password(passwordEncoder.encode(request.password()))
				.role(request.role() == null ? Role.USER : request.role())
				.build();

		User savedUser = userRepository.save(user);
		return new AuthResponse(jwtService.generateToken(savedUser), userMapper.toResponse(savedUser));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return new AuthResponse(jwtService.generateToken(user), userMapper.toResponse(user));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
