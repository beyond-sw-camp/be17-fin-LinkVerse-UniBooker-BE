package org.example.apireservation.domain.model;

import lombok.*;
import org.example.common.user.UserRole;

@Getter
@Setter
public class User {
    private Long id;
    private UserRole role;
    private String userName;
}