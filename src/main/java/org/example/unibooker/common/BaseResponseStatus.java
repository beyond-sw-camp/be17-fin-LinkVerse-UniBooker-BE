package org.example.unibooker.common;

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

    // ========== 30000: User 관련 ==========
    USER_NOT_FOUND(30000, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(30001, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(30002, "비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(30003, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),

    // ========== 40000: Company 관련 ==========
    COMPANY_NOT_FOUND(40000, "기업 정보를 찾을 수 없습니다."),
    DUPLICATE_COMPANY_NAME(40001, "이미 등록된 기업명입니다."),
    ALREADY_APPROVED(40002, "이미 승인된 기업입니다."),
    ALREADY_REJECTED(40003, "이미 거절된 신청입니다."),

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

    // ========== 60000: File 관련 ==========
    INVALID_FILE_TYPE(60000, "지원하지 않는 파일 형식입니다. (jpg, jpeg, png만 가능)"),
    FILE_SIZE_EXCEEDED(60001, "파일 크기는 5MB를 초과할 수 없습니다."),
    FILE_UPLOAD_FAILED(60002, "파일 업로드에 실패했습니다.");

    private final int code;
    private final String message;
}