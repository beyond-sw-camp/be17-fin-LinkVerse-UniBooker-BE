package org.example.common.exception;

import lombok.Getter;
import org.example.common.base.BaseResponseStatus;

@Getter
public class BaseException extends RuntimeException {

    private final BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }

    public int getCode() {
        return status.getCode();
    }

    public String getStatusMessage() {
        return status.getMessage();
    }
}
