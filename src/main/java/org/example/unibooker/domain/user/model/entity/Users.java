package org.example.unibooker.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.user.model.Gender;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.hibernate.annotations.Comment;

/**
 * 사용자 엔티티
 * - 일반 사용자, 관리자, 매니저, 슈퍼관리자 모두 포함
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_company",
                        columnNames = {"email", "company_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("사용자")
public class Users extends BaseEntity {

    @Column(nullable = false, length = 50)
    @Comment("이름")
    private String name;

    @Column(nullable = false, length = 100)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @Comment("기업")
    private Companies company;

    @Column(nullable = false)
    @Comment("첫 로그인 여부")
    private Boolean isFirstLogin = false;

    @Column(nullable = false)
    @Comment("기업 정지로 인한 계정 정지 여부")
    private Boolean suspendedByCompany = false;

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
     * 기업 ID 변경
     */
    public void updateCompany(Companies newCompany) {
        this.company = newCompany;
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
     * 기업 정지로 인한 계정 정지
     * - ACTIVE 상태일 때만 정지
     * - suspendedByCompany 플래그 설정
     */
    public void suspendByCompany() {
        if (this.status == UserStatus.ACTIVE) {
            this.status = UserStatus.SUSPENDED;
            this.suspendedByCompany = true;
        }
    }

    /**
     * 기업 활성화로 인한 계정 복구
     * - suspendedByCompany가 true일 때만 복구
     */
    public void restoreByCompany() {
        if (this.suspendedByCompany) {
            this.status = UserStatus.ACTIVE;
            this.suspendedByCompany = false;
        }
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
        this.isFirstLogin = true;
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

    // ========== Builder 패턴 (정적 팩토리) ==========

    /**
     * Users 엔티티 생성자
     */
    @Builder
    public Users(Long id, String email, String password, String name, String phone,
                 String birthDate, Gender gender, UserRole role, UserStatus status,
                 Companies company, Boolean isFirstLogin, Boolean suspendedByCompany) {
        super.setId(id);
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.company = company; // SUPER는 null, 나머지는 반드시 설정
        this.isFirstLogin = isFirstLogin != null ? isFirstLogin : false;
        this.suspendedByCompany = suspendedByCompany != null ? suspendedByCompany : false;
    }

    /**
     * SUPER 사용자 생성 (company = null)
     */
    public static Users createSuper(String email, String password, String name) {
        return Users.builder()
                .email(email)
                .password(password)
                .name(name)
                .role(UserRole.SUPER)
                .status(UserStatus.ACTIVE)
                .company(null)
                .isFirstLogin(false)
                .build();
    }

    /**
     * 기업 소속 사용자 생성 (ADMIN, MANAGER, USER)
     */
    public static Users createWithCompany(String email, String password, String name,
                                          Companies company, UserRole role) {
        if (company == null && role != UserRole.SUPER) {
            throw new IllegalArgumentException("SUPER 외 역할은 반드시 기업을 지정해야 합니다.");
        }
        return Users.builder()
                .email(email)
                .password(password)
                .name(name)
                .company(company)
                .role(role)
                .status(role == UserRole.ADMIN ? UserStatus.INACTIVE : UserStatus.ACTIVE)
                .isFirstLogin(role == UserRole.ADMIN || role == UserRole.MANAGER)
                .build();
    }
}