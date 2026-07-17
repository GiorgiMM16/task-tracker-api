package com.example.tasktracker.mapper;

import com.example.tasktracker.dto.project.ProjectResponse;
import com.example.tasktracker.dto.user.UserResponse;
import com.example.tasktracker.entity.Project;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-17T11:02:57+0400",
    comments = "version: 1.6.3, compiler: javac, environment: Java 26 (Oracle Corporation)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ProjectResponse toResponse(Project project) {
        if ( project == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String description = null;
        UserResponse owner = null;
        LocalDateTime createDate = null;
        LocalDateTime updateDate = null;

        id = project.getId();
        name = project.getName();
        description = project.getDescription();
        owner = userMapper.toResponse( project.getOwner() );
        createDate = project.getCreateDate();
        updateDate = project.getUpdateDate();

        ProjectResponse projectResponse = new ProjectResponse( id, name, description, owner, createDate, updateDate );

        return projectResponse;
    }
}
