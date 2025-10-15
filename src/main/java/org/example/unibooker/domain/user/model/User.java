package org.example.unibooker.domain.user.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("사용자")
public class User extends BaseEntity {

    @Column(nullable = false, length = 50)
    @Comment("이름")
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    @Comment("이메일")
    private String email;

    @Column(nullable = false, length = 255)
    @Comment("비밀번호")
    private String password;

    @Column(length = 20)
    @Comment("전화번호")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("역할")
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("상태")
    private UserStatus status;

    @Column(name = "company_id")
    @Comment("기업 ID")
    private Long companyId;

    @Column(nullable = false)
    @Comment("첫 로그인 여부")
    private Boolean isFirstLogin = false;

    @Builder
    public User(String email, String password, String name, String phone,
                UserRole role, UserStatus status, Long companyId, Boolean isFirstLogin) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.companyId = companyId;
        this.isFirstLogin = isFirstLogin != null ? isFirstLogin : false;
    }

    // 비즈니스 로직 메서드
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updatePhone(String newPhone) {
        this.phone = newPhone;
    }

    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void delete() {
        this.setDeletedAt(java.time.LocalDateTime.now());
        this.status = UserStatus.DELETED;
    }

    public void restore() {
        this.setDeletedAt(null);
        this.status = UserStatus.ACTIVE;
    }

    public void completeFirstLogin() {
        this.isFirstLogin = false;
    }

    // 상태 확인 메서드
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isInactive() {
        return this.status == UserStatus.INACTIVE;
    }

    public boolean isSuspended() {
        return this.status == UserStatus.SUSPENDED;
    }

    public boolean isDeleted() {
        return this.status == UserStatus.DELETED || this.getDeletedAt() != null;
    }

    // 권한 확인 메서드
    public boolean isUser() {
        return this.role == UserRole.USER;
    }

    public boolean isManager() {
        return this.role == UserRole.MANAGER;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isSuper() {
        return this.role == UserRole.SUPER;
    }

    // 권한 레벨 체크 (상위 권한 포함)
    public boolean hasSuperAuthority() {
        return this.role == UserRole.SUPER;
    }

    public boolean hasAdminAuthority() {
        return this.role == UserRole.ADMIN || hasSuperAuthority();
    }

    public boolean hasManagerAuthority() {
        return this.role == UserRole.MANAGER || hasAdminAuthority();
    }
}