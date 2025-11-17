package org.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.model.Gender;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;

import java.time.LocalDateTime;

/**
 * User 동기화 이벤트
 * - api-app에서 User 생성/수정/삭제 시 발행
 * - api-reservation에서 수신하여 Users 테이블 동기화
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncEvent {

    /**
     * 이벤트 타입
     */
    private EventType eventType;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime eventTime;

    // ========== User 기본 정보 ==========

    /**
     * 사용자 ID (PK)
     */
    private Long userId;

    /**
     * 이메일
     */
    private String email;

    /**
     * 비밀번호 (암호화된 상태)
     */
    private String password;

    /**
     * 이름
     */
    private String name;

    /**
     * 전화번호
     */
    private String phone;

    /**
     * 생년월일
     */
    private String birthDate;

    /**
     * 성별
     */
    private Gender gender;

    // ========== 기업 및 권한 정보 ==========

    /**
     * 기업 ID
     */
    private Long companyId;

    /**
     * 역할
     */
    private UserRole role;

    /**
     * 상태
     */
    private UserStatus status;

    // ========== 기타 정보 ==========

    /**
     * 첫 로그인 여부
     */
    private Boolean isFirstLogin;

    /**
     * 기업 정지로 인한 정지 여부
     */
    private Boolean suspendedByCompany;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (Soft Delete)
     */
    private LocalDateTime deletedAt;

    // ========== 팩토리 메서드 ==========

    /**
     * User 엔티티로부터 CREATED 이벤트 생성
     */
    public static UserSyncEvent fromUserCreated(
            Long userId,
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
        return UserSyncEvent.builder()
                .eventType(EventType.CREATED)
                .eventTime(LocalDateTime.now())
                .userId(userId)
                .email(email)
                .password(password)
                .name(name)
                .phone(phone)
                .birthDate(birthDate)
                .gender(gender)
                .companyId(companyId)
                .role(role)
                .status(status)
                .isFirstLogin(isFirstLogin)
                .suspendedByCompany(suspendedByCompany)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(null)
                .build();
    }

    /**
     * User 엔티티로부터 UPDATED 이벤트 생성
     */
    public static UserSyncEvent fromUserUpdated(
            Long userId,
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
        return UserSyncEvent.builder()
                .eventType(EventType.UPDATED)
                .eventTime(LocalDateTime.now())
                .userId(userId)
                .email(email)
                .password(password)
                .name(name)
                .phone(phone)
                .birthDate(birthDate)
                .gender(gender)
                .companyId(companyId)
                .role(role)
                .status(status)
                .isFirstLogin(isFirstLogin)
                .suspendedByCompany(suspendedByCompany)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(null)
                .build();
    }

    /**
     * User 엔티티로부터 DELETED 이벤트 생성
     */
    public static UserSyncEvent fromUserDeleted(
            Long userId,
            LocalDateTime deletedAt
    ) {
        return UserSyncEvent.builder()
                .eventType(EventType.DELETED)
                .eventTime(LocalDateTime.now())
                .userId(userId)
                .deletedAt(deletedAt)
                .build();
    }
}