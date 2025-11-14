package org.example.apireservation.domain.model;

import lombok.*;
import org.example.common.model.UserRole;

@Getter
@Builder
public class User {
    private Long id;
    private UserRole role;
    private String email;
    private String userName;
}