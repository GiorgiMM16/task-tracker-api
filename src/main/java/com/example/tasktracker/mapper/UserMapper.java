package com.example.tasktracker.mapper;

import com.example.tasktracker.dto.user.UserResponse;
import com.example.tasktracker.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserResponse toResponse(User user);
}
