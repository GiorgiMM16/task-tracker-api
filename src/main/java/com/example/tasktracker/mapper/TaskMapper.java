package com.example.tasktracker.mapper;

import com.example.tasktracker.dto.task.TaskResponse;
import com.example.tasktracker.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TaskMapper {

	@Mapping(source = "project.id", target = "projectId")
	@Mapping(source = "project.name", target = "projectName")
	TaskResponse toResponse(Task task);
}
