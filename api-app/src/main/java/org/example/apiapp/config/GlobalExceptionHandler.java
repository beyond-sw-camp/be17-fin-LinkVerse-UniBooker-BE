package org.example.apiapp.config;

import lombok.extern.slf4j.Slf4j;
import org.example.common.base.BaseResponse;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * - BaseException 처리
 * - Validation 예외 처리
 * - 기타 예외 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BaseException 처리
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        log.warn("BaseException 발생 - code: {}, message: {}", e.getCode(), e.getStatusMessage());

        BaseResponse<Void> response = BaseResponse.error(e.getCode(), e.getStatusMessage());

        // HTTP 상태 코드 결정
        HttpStatus status = determineHttpStatus(e.getCode());

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Validation 예외 처리 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {

        FieldError fieldError = e.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "잘못된 요청입니다.";

        log.warn("Validation 예외 발생 - message: {}", errorMessage);

        BaseResponse<Void> response = BaseResponse.error(
                BaseResponseStatus.BAD_REQUEST.getCode(),
                errorMessage
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        log.error("예상치 못한 예외 발생", e);

        BaseResponse<Void> response = BaseResponse.error(
                BaseResponseStatus.INTERNAL_SERVER_ERROR.getCode(),
                BaseResponseStatus.INTERNAL_SERVER_ERROR.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 에러 코드에 따른 HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatus(int code) {
        // 30000번대: 사용자 관련 에러 (대부분 400)
        if (code >= 30000 && code < 40000) {
            // 인증 실패 관련은 401
            if (code == 30011 || code == 30010) {  // INVALID_CREDENTIALS, INVALID_PASSWORD
                return HttpStatus.UNAUTHORIZED;
            }
            // 계정 상태 관련은 403
            if (code == 30020 || code == 30021 || code == 30022) {  // INACTIVE, SUSPENDED, DELETED
                return HttpStatus.FORBIDDEN;
            }
            return HttpStatus.BAD_REQUEST;
        }

        // 40000번대: 기업 관련 에러 (대부분 400)
        if (code >= 40000 && code < 50000) {
            // 승인 관련은 403
            if (code == 40010 || code == 40023) {  // COMPANY_NOT_APPROVED, COMPANY_SUSPENDED
                return HttpStatus.FORBIDDEN;
            }
            return HttpStatus.BAD_REQUEST;
        }

        // 50000번대: 인증/권한 에러
        if (code >= 50000 && code < 60000) {
            if (code == 50000) {  // UNAUTHORIZED
                return HttpStatus.UNAUTHORIZED;
            }
            return HttpStatus.FORBIDDEN;
        }

        // 기타는 400
        return HttpStatus.BAD_REQUEST;
    }
}