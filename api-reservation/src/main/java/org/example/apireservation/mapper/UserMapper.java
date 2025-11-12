package org.example.apireservation.mapper;

import org.example.apireservation.domain.model.User;
import org.example.apireservation.domain.model.entity.Users;

public class UserMapper {

    /** Entity -> Domain */
    public static User from(Users entity) {
        return User.builder()
                .id(entity.getId())
                .role(entity.getRole())
                .email(entity.getEmail())
                .userName(entity.getName())
                .build();
    }
}
