package com.ecommerce.common.exception;

import com.ecommerce.common.constant.CommonErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = CommonErrorCode.RESOURCE_NOT_FOUND;
        this.httpStatus = HttpStatus.NOT_FOUND;
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = CommonErrorCode.RESOURCE_NOT_FOUND;
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
