package org.example.unibooker.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    // 성공 응답 (데이터 있음)
    private BaseResponse(T data) {
        this.code = BaseResponseStatus.SUCCESS.getCode();
        this.message = BaseResponseStatus.SUCCESS.getMessage();
        this.data = data;
    }

    // 성공 응답 (데이터 없음)
    private BaseResponse() {
        this.code = BaseResponseStatus.SUCCESS.getCode();
        this.message = BaseResponseStatus.SUCCESS.getMessage();
        this.data = null;
    }

    // 에러 응답
    private BaseResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    // 에러 응답 (BaseResponseStatus)
    private BaseResponse(BaseResponseStatus status) {
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = null;
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
     * 에러 응답 (커스텀 코드, 메시지)
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message);
    }

    /**
     * 에러 응답 (BaseResponseStatus 사용)
     */
    public static <T> BaseResponse<T> error(BaseResponseStatus status) {
        return new BaseResponse<>(status);
    }
}