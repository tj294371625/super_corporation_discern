package com.chinadaas.component.mapper.annotation;

import java.lang.annotation.*;

/**
 * @author liubc
 * @version 1.0.0
 * @description 字段注解
 * @createTime 2021.07.01
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FieldType {

    /**
     * 对应neo4j结果中普通字段的键名
     *
     * @return
     */
    String value() default "";
}
