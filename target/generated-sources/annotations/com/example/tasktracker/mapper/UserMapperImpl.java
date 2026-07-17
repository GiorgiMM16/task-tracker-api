package com.example.tasktracker.mapper;

import com.example.tasktracker.dto.user.UserResponse;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.model.Role;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-17T11:02:57+0400",
    comments = "version: 1.6.3, compiler: javac, environment: Java 26 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String email = null;
        Role role = null;
        LocalDateTime createDate = null;
        LocalDateTime updateDate = null;

        id = user.getId();
        email = user.getEmail();
        role = user.getRole();
        createDate = user.getCreateDate();
        updateDate = user.getUpdateDate();

        UserResponse userResponse = new UserResponse( id, email, role, createDate, updateDate );

        return userResponse;
    }
}
