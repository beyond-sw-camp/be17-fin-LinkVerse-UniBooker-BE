package org.example.common.exception;

import lombok.Getter;
import org.example.common.base.BaseResponseStatus;

/**
 * 공통 예외 클래스
 * - RuntimeException 상속
 * - 상태 코드와 메시지 포함
 */
@Getter
public class BaseException extends RuntimeException {

    private final int code;
    private final String statusMessage;

    /**
     * 코드와 메시지로 예외 생성
     */
    public BaseException(int code, String message) {
        super(message);
        this.code = code;
        this.statusMessage = message;
    }

    /**
     * BaseResponseStatus로 예외 생성 (주로 사용)
     */
    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.code = status.getCode();
        this.statusMessage = status.getMessage();
    }
}