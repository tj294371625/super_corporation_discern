package com.chinadaas.component.mapper.annotation;


import java.lang.annotation.*;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 边注解
 * @createTime 2021.07.01
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LinkType {

    /**
     * 对应与边映射的键名
     *
     * @return
     */
    String value() default "";
}
