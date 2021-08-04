package com.chinadaas.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 断言
 * @createTime 2021.07.05
 */
@Slf4j
public abstract class Assert {

    /**
     * 如果实例为空，则报错
     *
     * @param obj         待检验的实例
     * @param message     报错信息
     * @param placeholder 占位符
     */
    public static void nonNull(Object obj, String message, Object... placeholder) {
        if (Objects.isNull(obj)) {
            log.error(message, placeholder);
            throw new RuntimeException();
        }
    }

    /**
     * 如果实例为空，则报错
     *
     * @param obj     待检验的实例
     * @param message 报错信息
     */
    public static void nonNull(Object obj, String message) {
        if (Objects.isNull(obj)) {
            log.error(message);
            throw new RuntimeException();
        }
    }

    /**
     * 判断传入的表达式是否为true
     *
     * @param expression 布尔表达式
     * @param message    报错信息
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            log.error(message);
            throw new RuntimeException();
        }
    }

    /**
     * 判断传入的表达式是否为false
     *
     * @param expression 布尔表达式
     * @param message    报错信息
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            log.error(message);
            throw new RuntimeException();
        }
    }

    /**
     * 如果字符串为空，则报错
     *
     * @param verifyStr 待检验的字符串
     * @param message   报错信息
     */
    public static void notBlank(String verifyStr, String message) {
        if (StringUtils.isBlank(verifyStr)) {
            log.error(message);
            throw new RuntimeException();
        }
    }
}
