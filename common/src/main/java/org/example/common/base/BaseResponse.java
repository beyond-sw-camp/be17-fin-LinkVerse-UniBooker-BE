package org.example.common.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * API 응답 공통 형식
 * - 성공/실패 여부
 * - 상태 코드
 * - 메시지
 * - 데이터
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    // ========== 생성자 ==========

    /**
     * 성공 응답 (데이터 있음)
     */
    private BaseResponse(T data) {
        this.code = 10000;
        this.message = "요청에 성공하였습니다.";
        this.data = data;
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    private BaseResponse() {
        this.code = 10000;
        this.message = "요청에 성공하였습니다.";
        this.data = null;
    }

    /**
     * 에러 응답 (커스텀)
     */
    private BaseResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    // ========== isSuccess 판단 ==========

    /**
     * 성공 여부 판단
     */
    @JsonProperty("isSuccess")
    public boolean isSuccess() {
        return this.code == 10000;
    }

    // ========== Static Factory Methods ==========

    /**
     * 성공 응답 (데이터 있음)
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data);
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>();
    }

    /**
     * 에러 응답 (코드, 메시지)
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message);
    }

    /**
     * 에러 응답 (BaseResponseStatus 사용)
     */
    public static <T> BaseResponse<T> error(BaseResponseStatus status) { return new BaseResponse<>(status.getCode(), status.getMessage()); }
}