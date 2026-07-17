package com.example.tasktracker.mapper;

import com.example.tasktracker.dto.project.ProjectResponse;
import com.example.tasktracker.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMapper {

	ProjectResponse toResponse(Project project);
}
