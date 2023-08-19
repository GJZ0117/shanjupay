package com.shanjupay.common.domain;

/**
 * 自定义异常类型
 */
public class BusinessException extends RuntimeException {

    private ErrorCode errorCode;

    public BusinessException() {

    }

    public BusinessException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
