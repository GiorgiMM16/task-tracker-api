package com.example.tasktracker.mapper;

import com.example.tasktracker.dto.task.TaskResponse;
import com.example.tasktracker.dto.user.UserResponse;
import com.example.tasktracker.entity.Project;
import com.example.tasktracker.entity.Task;
import com.example.tasktracker.model.Priority;
import com.example.tasktracker.model.TaskStatus;
import java.time.LocalDate;
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
public class TaskMapperImpl implements TaskMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public TaskResponse toResponse(Task task) {
        if ( task == null ) {
            return null;
        }

        Long projectId = null;
        String projectName = null;
        Long id = null;
        String title = null;
        String description = null;
        TaskStatus status = null;
        LocalDate dueDate = null;
        Priority priority = null;
        UserResponse assignedUser = null;
        LocalDateTime createDate = null;
        LocalDateTime updateDate = null;

        projectId = taskProjectId( task );
        projectName = taskProjectName( task );
        id = task.getId();
        title = task.getTitle();
        description = task.getDescription();
        status = task.getStatus();
        dueDate = task.getDueDate();
        priority = task.getPriority();
        assignedUser = userMapper.toResponse( task.getAssignedUser() );
        createDate = task.getCreateDate();
        updateDate = task.getUpdateDate();

        TaskResponse taskResponse = new TaskResponse( id, title, description, status, dueDate, priority, projectId, projectName, assignedUser, createDate, updateDate );

        return taskResponse;
    }

    private Long taskProjectId(Task task) {
        Project project = task.getProject();
        if ( project == null ) {
            return null;
        }
        return project.getId();
    }

    private String taskProjectName(Task task) {
        Project project = task.getProject();
        if ( project == null ) {
            return null;
        }
        return project.getName();
    }
}
