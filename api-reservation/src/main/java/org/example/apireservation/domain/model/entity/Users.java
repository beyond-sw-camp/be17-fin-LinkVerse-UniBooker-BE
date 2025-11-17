package org.example.apireservation.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.common.model.Gender;
import org.example.common.base.BaseEntity;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class Users extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private Boolean isFirstLogin;

    @Column(nullable = false)
    private Boolean suspendedByCompany;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    /**
     * 이벤트로부터 정보 업데이트
     */
    public void updateFromEvent(
            String email,
            String password,
            String name,
            String phone,
            String birthDate,
            Gender gender,
            Long companyId,
            UserRole role,
            UserStatus status,
            Boolean isFirstLogin,
            Boolean suspendedByCompany,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.companyId = companyId;
        this.role = role;
        this.status = status;
        this.isFirstLogin = isFirstLogin;
        this.suspendedByCompany = suspendedByCompany;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Soft Delete 처리
     */
    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.status = UserStatus.DELETED;
    }
}