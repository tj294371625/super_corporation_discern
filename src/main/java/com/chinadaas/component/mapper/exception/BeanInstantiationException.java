package com.chinadaas.component.mapper.exception;

/**
 * @author lawliet
 * @version 1.0.0
 * @description Bean实例化异常
 * @createTime 2021.07.03
 */
public class BeanInstantiationException extends RuntimeException{

    public BeanInstantiationException() {
    }

    public BeanInstantiationException(String message) {
        super(message);
    }

    public BeanInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanInstantiationException(Throwable cause) {
        super(cause);
    }

    public BeanInstantiationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
