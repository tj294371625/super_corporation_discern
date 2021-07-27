package com.chinadaas.component.mapper.annotation;

import java.lang.annotation.*;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 边集合注解
 * @createTime 2021.07.01
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LinksType {

    /**
     * 对应与关系列表映射的键名
     *
     * @return
     */
    String value() default "";
}
