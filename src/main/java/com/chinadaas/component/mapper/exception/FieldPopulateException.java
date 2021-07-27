package com.chinadaas.component.mapper.exception;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 字段填充异常
 * @createTime 2021.07.03
 */
public class FieldPopulateException extends RuntimeException {

    public FieldPopulateException() {
    }

    public FieldPopulateException(String message) {
        super(message);
    }

    public FieldPopulateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldPopulateException(Throwable cause) {
        super(cause);
    }

    public FieldPopulateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
