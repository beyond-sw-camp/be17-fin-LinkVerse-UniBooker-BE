package org.example.common.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 응답 상태 코드 및 메시지 정의
 *
 * 코드 체계:
 * - 10000: 성공
 * - 20000: 공통 에러
 * - 30000: 사용자 관련
 * - 40000: 기업 관련
 * - 50000: 인증/권한 관련
 * - 60000: 파일 관련
 * - 70000: 이메일 관련
 * - 80000: 토큰 관련
 * - 90000: 예약 관련
 * - 100000: 리소스 그룹 관련
 * - 200000: 리소스 관련
 */
@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {

    // ========== 10000: 성공 ==========
    SUCCESS(10000, "요청에 성공하였습니다."),

    // ========== 20000: 공통 에러 ==========
    BAD_REQUEST(20000, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(20001, "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(20002, "데이터베이스 오류가 발생했습니다."),
    CONCURRENT_MODIFICATION(20003, "다른 사용자가 동시에 수정 중입니다."),

    // ========== 30000: 사용자 관련 ==========
    // 조회
    USER_NOT_FOUND(30000, "사용자를 찾을 수 없습니다."),

    // 회원가입/중복
    DUPLICATE_EMAIL(30001, "이미 사용 중인 이메일입니다."),
    DUPLICATE_EMAIL_IN_COMPANY(30002, "해당 기업에 이미 등록된 이메일입니다."),
    ADMIN_MANAGER_EMAIL_EXISTS(30003, "해당 이메일은 이미 관리자 또는 매니저로 등록되어 있습니다."),
    MANAGER_ALREADY_EXISTS(30004, "해당 이메일로 이미 매니저가 등록되어 있습니다."),

    // 비밀번호
    INVALID_PASSWORD(30010, "비밀번호가 일치하지 않습니다."),
    INVALID_CREDENTIALS(30011, "이메일 또는 비밀번호가 일치하지 않습니다."),  // 로그인 실패 (보안)
    PASSWORD_MISMATCH(30012, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    SAME_PASSWORD(30013, "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    CURRENT_PASSWORD_INCORRECT(30014, "현재 비밀번호가 일치하지 않습니다."),

    // 계정 상태
    INACTIVE_USER(30020, "비활성화된 계정입니다."),
    ACCOUNT_SUSPENDED(30021, "정지된 계정입니다."),
    ACCOUNT_DELETED(30022, "탈퇴한 계정입니다."),
    ACCOUNT_REACTIVATED(30023, "탈퇴했던 계정이 재활성화되었습니다."),

    // 권한/역할
    INVALID_USER_STATUS(30030, "유효하지 않은 사용자 상태입니다."),
    INVALID_USER_ROLE(30031, "유효하지 않은 사용자 권한입니다."),
    ROLE_CONFLICT_IN_COMPANY(30032, "같은 기업 내에서 관리자와 매니저 역할을 동시에 가질 수 없습니다."),
    NOT_MANAGER(30033, "매니저 권한이 아닙니다."),

    // ========== 40000: 기업 관련 ==========
    // 조회
    COMPANY_NOT_FOUND(40000, "기업 정보를 찾을 수 없습니다."),

    // 중복
    DUPLICATE_COMPANY_NAME(40001, "이미 등록된 기업명입니다."),
    DUPLICATE_BUSINESS_NUMBER(40002, "이미 등록된 사업자등록번호입니다."),
    DUPLICATE_SLUG(40003, "이미 사용 중인 Company Slug입니다."),

    // 승인 관련
    COMPANY_NOT_APPROVED(40010, "승인되지 않은 기업입니다."),
    ALREADY_APPROVED(40011, "이미 승인된 기업입니다."),
    ALREADY_REJECTED(40012, "이미 거절된 신청입니다."),

    // 상태 관련
    INVALID_COMPANY_STATUS(40020, "유효하지 않은 기업 상태입니다."),
    INVALID_STATUS_CHANGE(40021, "변경할 수 없는 상태입니다."),
    COMPANY_NOT_ACTIVE(40022, "활성 상태의 기업만 정지할 수 있습니다."),
    COMPANY_SUSPENDED(40023, "서비스가 일시 정지되었습니다."),

    // Slug 관련
    INVALID_SLUG_FORMAT(40030, "Company Slug 형식이 올바르지 않습니다."),
    RESERVED_SLUG(40031, "사용할 수 없는 예약어입니다."),

    // 사업자등록번호
    INVALID_BUSINESS_NUMBER_FORMAT(40040, "올바른 사업자등록번호 형식이 아닙니다."),

    // ========== 50000: 인증/권한 관련 ==========
    // 인증
    UNAUTHORIZED(50000, "인증되지 않은 사용자입니다."),
    FORBIDDEN(50001, "접근 권한이 없습니다."),
    UNAUTHORIZED_ACTION(50002, "해당 작업을 수행할 권한이 없습니다."),
    UNAUTHORIZED_COMPANY_ACCESS(50003, "해당 기업의 리소스에 접근 권한이 없습니다."),

    // 토큰
    INVALID_TOKEN(50010, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(50011, "만료된 토큰입니다."),

    // 로그인 상태
    PASSWORD_CHANGE_REQUIRED(50020, "비밀번호 변경이 필요합니다."),
    APPROVAL_PENDING(50021, "승인 대기 중입니다."),
    APPROVAL_REJECTED(50022, "가입 신청이 거절되었습니다."),

    // ========== 60000: 파일 관련 ==========
    INVALID_FILE_TYPE(60000, "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(60001, "파일 크기가 제한을 초과했습니다."),
    FILE_UPLOAD_FAILED(60002, "파일 업로드에 실패했습니다."),

    // ========== 70000: 이메일 관련 ==========
    EMAIL_SEND_FAILED(70000, "이메일 발송에 실패했습니다."),

    // ========== 80000: Refresh Token 관련 ==========
    REFRESH_TOKEN_EXPIRED(80000, "Refresh Token이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(80001, "Refresh Token을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(80002, "유효하지 않은 Refresh Token입니다."),

    // ========== 90000: 예약 관련 ==========
    RESERVATION_NOT_FOUND(90000, "예약 정보를 찾을 수 없습니다."),
    RESERVATION_CANCEL_FAILED(90001, "예약 취소에 실패했습니다."),
    RESERVATION_DATE_RANGE_OVER(90002, "예약 가능한 날짜 범위를 벗어났습니다."),
    RESERVATION_DUPLICATED(90003, "중복된 예약이 있습니다."),
    RESERVATION_ALREADY_CANCELED(90004, "이미 취소된 예약입니다."),

    // ========== 100000: 리소스 그룹 관련 ==========
    RESOURCE_GROUP_NOT_FOUND(100000, "리소스 그룹을 찾을 수 없습니다."),
    INVALID_SERVICE_CATEGORY(100001, "유효하지 않은 카테고리입니다."),
    RESOURCE_BUSY(100002, "해당 리소스는 예약 중입니다."),

    // ========== 200000: 리소스 관련 ==========
    RESOURCE_NOT_FOUND(200000, "리소스를 찾을 수 없습니다."),
    RESOURCE_OVER_CAPACITY(200001, "정원이 초과되어 예약할 수 없습니다."),
    RESOURCE_NOT_ACTIVE(200002, "활성화되지 않은 리소스입니다."),
    RESOURCE_STATUS_CHANGE_FAILED(200003, "리소스 상태 변경에 실패했습니다.");

    private final int code;
    private final String message;
}