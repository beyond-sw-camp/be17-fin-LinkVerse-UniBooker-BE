package org.example.unibooker.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.user.model.Gender;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.hibernate.annotations.Comment;

/**
 * 사용자 엔티티
 * - 일반 사용자, 관리자, 매니저, 슈퍼관리자 모두 포함
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("사용자")
public class Users extends BaseEntity {

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

    @Column(name = "birth_date", length = 20)
    @Comment("생년월일")
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Comment("성별")
    private Gender gender;

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

    /**
     * User 생성자 (Builder 패턴)
     */
    @Builder
    public Users(Long id, String email, String password, String name, String phone,
                 String birthDate, Gender gender,
                 UserRole role, UserStatus status, Long companyId, Boolean isFirstLogin) {
        super.setId(id);
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.companyId = companyId;
        this.isFirstLogin = isFirstLogin != null ? isFirstLogin : false;
    }

    // ========== 비즈니스 로직 메서드 ==========

    /**
     * 비밀번호 변경
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 이름 변경
     */
    public void updateName(String newName) {
        if (newName != null && !newName.isBlank()) {
            this.name = newName;
        }
    }

    /**
     * 연락처 변경
     */
    public void updatePhone(String newPhone) {
        this.phone = newPhone;
    }

    /**
     * 생년월일 변경
     */
    public void updateBirthDate(String newBirthDate) {
        this.birthDate = newBirthDate;
    }

    /**
     * 성별 변경
     */
    public void updateGender(Gender newGender) {
        this.gender = newGender;
    }

    /**
     * 권한 변경
     */
    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * 계정 정지
     */
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    /**
     * 계정 삭제 (소프트 삭제)
     */
    public void delete() {
        this.setDeletedAt(java.time.LocalDateTime.now());
        this.status = UserStatus.DELETED;
    }

    /**
     * 계정 복구
     */
    public void restore() {
        this.setDeletedAt(null);
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 첫 로그인 완료 처리
     */
    public void completeFirstLogin() {
        this.isFirstLogin = false;
    }

    // ========== 상태 확인 메서드 ==========

    /**
     * 활성 상태 확인
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * 비활성 상태 확인
     */
    public boolean isInactive() {
        return this.status == UserStatus.INACTIVE;
    }

    /**
     * 정지 상태 확인
     */
    public boolean isSuspended() {
        return this.status == UserStatus.SUSPENDED;
    }

    /**
     * 삭제 상태 확인
     */
    public boolean isDeleted() {
        return this.status == UserStatus.DELETED || this.getDeletedAt() != null;
    }

    // ========== 권한 확인 메서드 ==========

    /**
     * 일반 사용자 권한 확인
     */
    public boolean isUser() {
        return this.role == UserRole.USER;
    }

    /**
     * 매니저 권한 확인
     */
    public boolean isManager() {
        return this.role == UserRole.MANAGER;
    }

    /**
     * 관리자 권한 확인
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * 슈퍼관리자 권한 확인
     */
    public boolean isSuper() {
        return this.role == UserRole.SUPER;
    }

    /**
     * 슈퍼관리자 권한 보유 여부
     */
    public boolean hasSuperAuthority() {
        return this.role == UserRole.SUPER;
    }

    /**
     * 관리자 이상 권한 보유 여부
     */
    public boolean hasAdminAuthority() {
        return this.role == UserRole.ADMIN || hasSuperAuthority();
    }

    /**
     * 매니저 이상 권한 보유 여부
     */
    public boolean hasManagerAuthority() {
        return this.role == UserRole.MANAGER || hasAdminAuthority();
    }
}