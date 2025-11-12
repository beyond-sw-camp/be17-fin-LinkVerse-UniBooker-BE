package org.example.apireservation.domain.model.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.example.common.base.BaseEntity;
import org.example.common.user.UserRole;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users extends BaseEntity {
    private String name;
    private String email;
    private UserRole role;
}