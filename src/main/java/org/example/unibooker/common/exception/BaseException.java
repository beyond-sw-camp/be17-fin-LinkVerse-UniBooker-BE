package org.example.unibooker.common.exception;

import lombok.Getter;
import org.example.unibooker.common.BaseResponseStatus;

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
