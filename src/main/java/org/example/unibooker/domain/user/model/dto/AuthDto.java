package org.example.unibooker.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.unibooker.domain.user.model.Gender;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.Users;

import java.time.LocalDateTime;

/**
 * 인증된 사용자 정보 DTO
 * - 다른 도메인(예약, 리소스 등)에서 인증된 사용자 정보 사용
 * - Users 엔티티의 모든 정보 포함
 */
public class AuthDto {

    // ========== 공통 추상 클래스 ==========

    /**
     * 인증된 사용자 기본 정보 (Users 엔티티의 모든 필드 포함)
     */
    @Getter
    @Schema(description = "인증된 사용자 기본 정보")
    public abstract static class AuthenticatedUser {

        // ===== BaseEntity 필드 =====

        @Schema(description = "사용자 ID (PK)", example = "1")
        private final Long id;

        @Schema(description = "생성 일시", example = "2025-10-16T14:30:00")
        private final LocalDateTime createdAt;

        @Schema(description = "수정 일시", example = "2025-10-16T15:00:00")
        private final LocalDateTime updatedAt;

        @Schema(description = "삭제 일시 (소프트 삭제)", example = "null")
        private final LocalDateTime deletedAt;

        // ===== Users 엔티티 필드 =====

        @Schema(description = "이름", example = "홍길동")
        private final String name;

        @Schema(description = "이메일", example = "user@example.com")
        private final String email;

        @Schema(description = "암호화된 비밀번호 (내부용)", hidden = true)
        private final String password;

        @Schema(description = "전화번호", example = "010-1234-5678")
        private final String phone;

        @Schema(description = "생년월일 (YYYY-MM-DD)", example = "1990-01-15")
        private final String birthDate;

        @Schema(description = "성별", example = "MALE")
        private final Gender gender;

        @Schema(description = "권한", example = "USER")
        private final UserRole role;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private final UserStatus status;

        @Schema(description = "소속 기업 ID", example = "10")
        private final Long companyId;

        @Schema(description = "첫 로그인 여부", example = "false")
        private final Boolean isFirstLogin;

        /**
         * 생성자 (비밀번호 포함)
         */
        protected AuthenticatedUser(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                                    LocalDateTime deletedAt, String name, String email,
                                    String password, String phone, String birthDate, Gender gender,
                                    UserRole role, UserStatus status, Long companyId,
                                    Boolean isFirstLogin) {
            this.id = id;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.deletedAt = deletedAt;
            this.name = name;
            this.email = email;
            this.password = password;
            this.phone = phone;
            this.birthDate = birthDate;
            this.gender = gender;
            this.role = role;
            this.status = status;
            this.companyId = companyId;
            this.isFirstLogin = isFirstLogin;
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
            return this.status == UserStatus.DELETED || this.deletedAt != null;
        }

        /**
         * 특정 기업 소속 여부 확인
         */
        public boolean belongsToCompany(Long companyId) {
            return this.companyId != null && this.companyId.equals(companyId);
        }

        // ========== 권한 확인 메서드 ==========

        /**
         * 권한 확인
         */
        public boolean hasRole(UserRole role) {
            return this.role == role;
        }

        /**
         * 일반 사용자 권한 여부
         */
        public boolean isUser() {
            return this.role == UserRole.USER;
        }

        /**
         * 매니저 권한 여부
         */
        public boolean isManager() {
            return this.role == UserRole.MANAGER;
        }

        /**
         * 관리자 권한 여부
         */
        public boolean isAdmin() {
            return this.role == UserRole.ADMIN;
        }

        /**
         * 슈퍼관리자 권한 여부
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

        /**
         * 첫 로그인 여부
         */
        public boolean isFirstLogin() {
            return this.isFirstLogin != null && this.isFirstLogin;
        }

        // ========== 공통 검증 메서드 추가 ==========

        /**
         * 기본 활동 가능 여부 (활성 상태 + 삭제되지 않음)
         */
        protected boolean canPerformAction() {
            return isActive() && !isDeleted();
        }

        /**
         * 특정 기업 데이터 접근 가능 여부
         */
        protected boolean canAccessCompanyData(Long companyId) {
            return canPerformAction() && belongsToCompany(companyId);
        }

        // ========== 추상 메서드로 권한별 차이 구현 ==========

        /**
         * 리소스 관리 권한 (하위 클래스에서 구현)
         */
        public abstract boolean canManageResource(Long resourceCompanyId);

        /**
         * 분석 데이터 조회 권한 (하위 클래스에서 구현)
         */
        public abstract boolean canViewAnalytics(Long companyId);
    }

    // ========== 일반 사용자 ==========

    /**
     * 인증된 일반 사용자 (USER)
     */
    @Getter
    @Schema(description = "인증된 일반 사용자")
    public static class AuthUser extends AuthenticatedUser {

        private AuthUser(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                         LocalDateTime deletedAt, String name, String email,
                         String password, String phone, String birthDate, Gender gender,
                         UserRole role, UserStatus status, Long companyId,
                         Boolean isFirstLogin) {
            super(id, createdAt, updatedAt, deletedAt, name, email,
                    password, phone, birthDate, gender, role, status, companyId, isFirstLogin);
        }

        /**
         * Users 엔티티 → AuthUser 변환
         */
        public static AuthUser from(Users user) {
            return new AuthUser(
                    user.getId(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getDeletedAt(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getPhone(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getRole(),
                    user.getStatus(),
                    user.getCompany().getId(),
                    user.getIsFirstLogin()
            );
        }

        // ========== 추상 메서드 구현 ==========

        /**
         * 일반 사용자는 리소스 관리 불가
         */
        @Override
        public boolean canManageResource(Long resourceCompanyId) {
            return false;
        }

        /**
         * 일반 사용자는 분석 데이터 조회 불가
         */
        @Override
        public boolean canViewAnalytics(Long companyId) {
            return false;
        }

        // ========== USER 전용 메서드 (기존 유지) ==========

        /**
         * 예약 가능 여부
         */
        public boolean canReserve() {
            return canPerformAction();
        }

        /**
         * 본인 예약 취소 가능 여부
         */
        public boolean canCancelOwnReservation(Long reservationUserId) {
            return this.getId().equals(reservationUserId) && isActive();
        }

        /**
         * 본인 예약 조회 가능 여부
         */
        public boolean canViewOwnReservation(Long reservationUserId) {
            return this.getId().equals(reservationUserId);
        }
    }

    // ========== 매니저 ==========

    /**
     * 인증된 매니저 (MANAGER)
     */
    @Getter
    @Schema(description = "인증된 매니저")
    public static class AuthManager extends AuthenticatedUser implements AdminLike {

        private AuthManager(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                            LocalDateTime deletedAt, String name, String email,
                            String password, String phone, String birthDate, Gender gender,
                            UserRole role, UserStatus status, Long companyId,
                            Boolean isFirstLogin) {
            super(id, createdAt, updatedAt, deletedAt, name, email,
                    password, phone, birthDate, gender, role, status, companyId, isFirstLogin);
        }

        /**
         * Users 엔티티 → AuthManager 변환
         */
        public static AuthManager from(Users user) {
            if (user.getCompany().getId() == null) {
                throw new IllegalStateException("매니저는 반드시 기업에 소속되어야 합니다.");
            }

            return new AuthManager(
                    user.getId(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getDeletedAt(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getPhone(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getRole(),
                    user.getStatus(),
                    user.getCompany().getId(),
                    user.getIsFirstLogin()
            );
        }

        // ========== 추상 메서드 구현 ==========

        /**
         * 자신의 기업 리소스만 관리 가능
         */
        @Override
        public boolean canManageResource(Long resourceCompanyId) {
            return canAccessCompanyData(resourceCompanyId);
        }

        /**
         * 자신의 기업 분석 데이터만 조회 가능
         */
        @Override
        public boolean canViewAnalytics(Long companyId) {
            return canAccessCompanyData(companyId);
        }

        // ========== MANAGER 전용 메서드 (기존 유지) ==========

        /**
         * 관리 중인 기업 ID (명확성을 위한 별칭)
         */
        public Long getManagedCompanyId() {
            return this.getCompanyId();
        }

        /**
         * 특정 예약 관리 권한 확인
         */
        public boolean canManageReservation(Long reservationCompanyId) {
            return canAccessCompanyData(reservationCompanyId);
        }

        /**
         * 기업 데이터 조회 권한
         */
        public boolean canViewCompanyData() {
            return canPerformAction() && this.getCompanyId() != null;
        }

        /**
         * 기업 예약 현황 조회 권한
         */
        public boolean canViewCompanyReservations() {
            return canPerformAction() && this.getCompanyId() != null;
        }

        /**
         * 기업 통계 조회 권한 (새로운 메서드명으로 별칭 제공)
         */
        public boolean canViewCompanyAnalytics() {
            return canViewAnalytics(this.getCompanyId());
        }
    }

    /**
     * ADMIN/MANAGER 공통 인터페이스
     * - 관리자 권한이 필요한 메서드에서 공통으로 사용
     * - @AuthenticationPrincipal에서 타입 안정성 확보
     */
    public interface AdminLike {
        Long getId();
        String getEmail();
        UserRole getRole();
        Long getCompanyId();
        UserStatus getStatus();
        Boolean getIsFirstLogin();

        /**
         * 활성 상태 확인
         */
        boolean isActive();

        /**
         * 특정 기업 소속 여부 확인
         */
        boolean belongsToCompany(Long companyId);
    }

    // ========== 관리자 ==========

    /**
     * 인증된 관리자 (ADMIN, SUPER)
     */
    @Getter
    @Schema(description = "인증된 관리자")
    public static class AuthAdmin extends AuthenticatedUser implements AdminLike {

        @Schema(description = "슈퍼관리자 여부", example = "false")
        private final boolean isSuper;

        private AuthAdmin(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                          LocalDateTime deletedAt, String name, String email,
                          String password, String phone, String birthDate, Gender gender,
                          UserRole role, UserStatus status, Long companyId,
                          Boolean isFirstLogin) {
            super(id, createdAt, updatedAt, deletedAt, name, email,
                    password, phone, birthDate, gender, role, status, companyId, isFirstLogin);
            this.isSuper = (role == UserRole.SUPER);
        }

        /**
         * Users 엔티티 → AuthAdmin 변환
         */
        public static AuthAdmin from(Users user) {

            Long companyId = null;
            if (user.getCompany() != null) {
                companyId = user.getCompany().getId();
            }

            return new AuthAdmin(
                    user.getId(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getDeletedAt(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getPhone(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getRole(),
                    user.getStatus(),
                    companyId,
                    user.getIsFirstLogin()
            );
        }

        // ========== 추상 메서드 구현 ==========

        /**
         * 리소스 관리 권한
         * - SUPER: 모든 리소스 관리 가능
         * - ADMIN: 자신의 기업 리소스만
         */
        @Override
        public boolean canManageResource(Long resourceCompanyId) {
            if (!canPerformAction()) {
                return false;
            }
            return isSuper || belongsToCompany(resourceCompanyId);
        }

        /**
         * 분석 데이터 조회 권한
         * - SUPER: 모든 기업 데이터 조회 가능
         * - ADMIN: 자신의 기업 데이터만
         */
        @Override
        public boolean canViewAnalytics(Long companyId) {
            if (!canPerformAction()) {
                return false;
            }
            return isSuper || belongsToCompany(companyId);
        }

        // ========== ADMIN 전용 메서드 (기존 유지) ==========

        /**
         * 모든 리소스 관리 권한 (SUPER만)
         */
        public boolean canManageAllResources() {
            return canPerformAction() && isSuper;
        }

        /**
         * 특정 기업 관리 권한
         * - ADMIN: 자신의 기업만
         * - SUPER: 모든 기업
         */
        public boolean canManageCompany(Long companyId) {
            if (!canPerformAction()) {
                return false;
            }

            if (isSuper) {
                return true;
            }

            return belongsToCompany(companyId);
        }

        /**
         * 기업 승인 권한 (SUPER만)
         */
        public boolean canApproveCompany() {
            return canPerformAction() && isSuper;
        }

        /**
         * 사용자 관리 권한
         * - ADMIN: 자사 사용자만
         * - SUPER: 모든 사용자
         */
        public boolean canManageUsers() {
            return canPerformAction() && (isAdmin() || isSuper);
        }

        /**
         * 특정 기업의 사용자 관리 권한
         */
        public boolean canManageCompanyUsers(Long companyId) {
            if (!canPerformAction()) {
                return false;
            }

            if (isSuper) {
                return true;
            }

            return belongsToCompany(companyId);
        }

        /**
         * 매니저 생성 권한
         */
        public boolean canCreateManager() {
            return canPerformAction() && (isAdmin() || isSuper);
        }

        /**
         * 특정 기업의 분석 데이터 조회 권한 (별칭 메서드)
         */
        public boolean canViewCompanyAnalytics(Long companyId) {
            return canViewAnalytics(companyId);
        }
    }

    // ========== Refresh Token 관련 DTO ==========

    /**
     * Refresh Token 응답 DTO
     * - Access Token은 HttpOnly Cookie로 전달되므로 Response Body에서 제외
     */
    @Getter
    @Builder
    @Schema(description = "Refresh Token 갱신 응답")
    public static class RefreshTokenResponse {

        @Schema(description = "사용자 ID", example = "123")
        private Long userId;

        @Schema(description = "갱신 성공 메시지", example = "Access Token이 갱신되었습니다.")
        private String message;
    }

    /**
     * Refresh Token 응답 DTO (내부 전달용)
     * - Service → Controller 간 토큰 전달
     * - Controller에서 Cookie 설정 후 RefreshTokenResponse로 변환
     */
    @Getter
    @Builder
    @Schema(hidden = true, description = "Refresh Token 갱신 응답 (내부 전달용)")
    public static class RefreshTokenResponseWithToken {

        // ===== 토큰 (내부 전달용) =====

        private String accessToken;
        private UserRole role;  // ← 추가

        // @Schema(description = "새로운 Refresh Token (Rotation 적용 시)")
        // private String refreshToken;  // Rotation 적용 시 활성화

        // ===== 클라이언트 응답 필드 =====

        private Long userId;
        private String message;

        /**
         * 클라이언트 응답 DTO로 변환
         * - 토큰 제외한 정보만 반환
         */
        public RefreshTokenResponse toResponse() {
            return RefreshTokenResponse.builder()
                    .userId(this.userId)
                    .message(this.message)
                    .build();
        }
    }

    /**
     * 로그아웃 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "로그아웃 응답")
    public static class LogoutResponse {

        @Schema(description = "성공 메시지", example = "로그아웃되었습니다.")
        private String message;

        @Schema(description = "로그아웃 일시", example = "2025-10-25T15:00:00")
        private LocalDateTime logoutAt;  // ← 추가
    }
}