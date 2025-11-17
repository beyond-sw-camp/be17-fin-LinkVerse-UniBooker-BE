package org.example.common.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {

    // ========== 10000: 성공 ==========
    SUCCESS(10000, "요청에 성공하였습니다."),

    // ========== 20000: Common Error ==========
    BAD_REQUEST(20000, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(20001, "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(20002, "데이터베이스 오류가 발생했습니다."),
    CONCURRENT_MODIFICATION(20004, "다른 사용자가 동시에 수정을 요청했습니다."),


    // ========== 30000: User 관련 ==========
    USER_NOT_FOUND(30000, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(30001, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(30002, "비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(30003, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    INVALID_USER_STATUS(30004, "유효하지 않은 사용자 상태입니다."),
    INVALID_USER_ROLE(30005, "유효하지 않은 사용자 권한입니다."),
    DUPLICATE_EMAIL_IN_COMPANY(30006, "해당 기업에 이미 등록된 이메일입니다."),
    DELETED_USER(30007, "삭제된 계정입니다."),
    INACTIVE_USER(30008, "비활성화된 계정입니다."),
    SUSPENDED_USER(30009, "정지된 계정입니다."),
    SAME_PASSWORD(30010, "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    CURRENT_PASSWORD_INCORRECT(30011, "현재 비밀번호가 일치하지 않습니다."),
    ROLE_CONFLICT_IN_COMPANY(30012, "같은 기업 내에서 관리자와 매니저 역할을 동시에 가질 수 없습니다."),
    MANAGER_ALREADY_EXISTS(30013, "해당 이메일로 이미 매니저가 등록되어 있습니다."),
    ADMIN_MANAGER_EMAIL_EXISTS(30014, "해당 이메일은 이미 관리자 또는 매니저로 등록되어 있습니다."),

    // ========== 40000: Company 관련 ==========
    COMPANY_NOT_FOUND(40000, "기업 정보를 찾을 수 없습니다."),
    DUPLICATE_COMPANY_NAME(40001, "이미 등록된 기업명입니다."),
    ALREADY_APPROVED(40002, "이미 승인된 기업입니다."),
    ALREADY_REJECTED(40003, "이미 거절된 신청입니다."),
    COMPANY_NOT_APPROVED(40004, "승인되지 않은 기업입니다. 승인 후 매니저를 생성할 수 있습니다."),
    DUPLICATE_BUSINESS_NUMBER(40005, "이미 등록된 사업자등록번호입니다."),
    INVALID_BUSINESS_NUMBER_FORMAT(40006, "올바른 사업자등록번호 형식이 아닙니다."),

    // ==========  Company Slug 관련 ==========
    INVALID_SLUG_FORMAT(40007, "Company Slug 형식이 올바르지 않습니다. (소문자, 숫자, 하이픈만 가능, 3-30자)"),
    RESERVED_SLUG(40008, "사용할 수 없는 예약어입니다."),
    DUPLICATE_SLUG(40009, "이미 사용 중인 Company Slug입니다."),

    // ========== 기업 상태 관련 (신규 추가) ==========
    INVALID_COMPANY_STATUS(40010, "유효하지 않은 기업 상태입니다."),
    INVALID_STATUS_CHANGE(40011, "변경할 수 없는 상태입니다. PENDING과 REJECTED 상태는 변경할 수 없습니다."),
    COMPANY_NOT_ACTIVE(40012, "활성 상태의 기업만 정지할 수 있습니다."),
    COMPANY_SUSPENDED(40013, "서비스가 일시 정지되었습니다."),

    // ========== 50000: Auth 관련 (향후 로그인 구현 시 사용) ==========
    UNAUTHORIZED(50000, "인증되지 않은 사용자입니다."),
    FORBIDDEN(50001, "접근 권한이 없습니다."),
    INVALID_TOKEN(50002, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(50003, "만료된 토큰입니다."),
    PASSWORD_CHANGE_REQUIRED(50004, "비밀번호 변경이 필요합니다."),
    APPROVAL_PENDING(50005, "승인 대기 중입니다. 승인 후 로그인이 가능합니다."),
    APPROVAL_REJECTED(50006, "가입 신청이 거절되었습니다."),
    ACCOUNT_SUSPENDED(50007, "정지된 계정입니다."),
    ACCOUNT_DELETED(50008, "탈퇴한 계정입니다."),
    UNAUTHORIZED_ACTION(50009, "해당 작업을 수행할 권한이 없습니다."),
    UNAUTHORIZED_COMPANY_ACCESS(50010, "해당 기업의 리소스에 접근 권한이 없습니다."),


    // ========== 60000: File 관련 ==========
    INVALID_FILE_TYPE(60000, "지원하지 않는 파일 형식입니다. (jpg, jpeg, png만 가능)"),
    FILE_SIZE_EXCEEDED(60001, "파일 크기는 5MB를 초과할 수 없습니다."),
    FILE_UPLOAD_FAILED(60002, "파일 업로드에 실패했습니다."),

    // ========== 70000: Email 관련 ==========
    EMAIL_SEND_FAILED(70000, "이메일 발송에 실패했습니다."),

    // ========== 80000: Refresh Token 관련 ==========
    REFRESH_TOKEN_EXPIRED(80000, "Refresh Token이 만료되었습니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_NOT_FOUND(80001, "Refresh Token을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(80002, "유효하지 않은 Refresh Token입니다."),

    // ========== 90000: Reservation 관련 ==========
    RESERVATION_NOT_FOUND(90000, "예약 정보를 찿을 수 없습니다."),
    RESERVATION_CANCEL_FAILED(90001, "예약 취소 실패했습니다."),
    RESERVATION_DATE_RANGE_OVER(90002, "예약할 수 있는 날짜의 범위를 벗어났습니다."),
    RESERVATION_DUPLICATED(90003, "중복된 예약이 있습니다."),
    RESERVATION_ALREADY_CANCELED(90004, "이미 취소된 예약입니다."),

    // ========== 100000: ResourceGroup 관련 ==========
    RESOURCE_GROUP_NOT_FOUND(100001, "리소스 그룹 정보를 찾을 수 없습니다."),
    INVALID_SERVICE_CATEGORY(100002, "유효하지 않는 카테고리입니다."),
    RESOURCE_BUSY(100003, "해당 리소스는 예약 중인 슬롯입니다."),

    // ========== 200000: Resource 관련 ==========
    RESOURCE_NOT_FOUND(200000, "리소스 정보를 찾을 수 없습니다."),
    RESOURCE_OVER_CAPACITY(200001, "정원이 초과되어 예약 불가합니다."),
    RESOURCE_NOT_ACTIVE(200002, "활성화 되지 않은 리소스입니다."),
    RESOURCE_STATUS_CHANGE_FAILED(200003, "유효하지 않은 상태 변경 요청입니다.");


    private final int code;
    private final String message;
}