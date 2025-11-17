package org.example.apireservation.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.apireservation.domain.model.Gender;
import org.example.common.base.BaseEntity;
import org.example.common.model.UserRole;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users extends BaseEntity {
    private Long companyId;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String birthDate; // LocalDate
}