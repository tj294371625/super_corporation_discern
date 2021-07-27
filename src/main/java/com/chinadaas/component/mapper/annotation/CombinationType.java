package com.chinadaas.component.mapper.annotation;

import java.lang.annotation.*;

/**
 * @author liubc
 * @version 1.0.0
 * @description 组合注解
 * @createTime 2021.07.01
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CombinationType {

    /**
     * 对应neo4j结果中 点或边 的键名
     *
     * @return
     */
    String value() default "";
}
