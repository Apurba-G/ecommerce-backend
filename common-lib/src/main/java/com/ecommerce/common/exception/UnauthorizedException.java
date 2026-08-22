package com.ecommerce.common.exception;

import com.ecommerce.common.constant.CommonErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public UnauthorizedException(String message) {
        super(message);
        this.errorCode = CommonErrorCode.UNAUTHORIZED;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }

    public UnauthorizedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
}
