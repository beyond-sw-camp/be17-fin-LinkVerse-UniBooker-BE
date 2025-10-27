package org.example.unibooker.common.exception;

import org.example.unibooker.common.BaseResponseStatus;

/**
 * Refresh Token 관련 예외
 */
public class RefreshTokenException extends BaseException {

    public RefreshTokenException(BaseResponseStatus status) {
        super(status);
    }

    /**
     * Refresh Token 만료 예외
     */
    public static class RefreshTokenExpiredException extends RefreshTokenException {
        public RefreshTokenExpiredException() {
            super(BaseResponseStatus.REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * Refresh Token 없음 예외
     */
    public static class RefreshTokenNotFoundException extends RefreshTokenException {
        public RefreshTokenNotFoundException() {
            super(BaseResponseStatus.REFRESH_TOKEN_NOT_FOUND);
        }
    }

    /**
     * 잘못된 Refresh Token 예외
     */
    public static class InvalidRefreshTokenException extends RefreshTokenException {
        public InvalidRefreshTokenException() {
            super(BaseResponseStatus.INVALID_REFRESH_TOKEN);
        }
    }
}