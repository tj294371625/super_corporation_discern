package com.chinadaas.component.mapper.annotation;

import java.lang.annotation.*;

/**
 * @author liubc
 * @version 1.0.0
 * @description 节点集合注解
 * @createTime 2021.07.01
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface NodesType {

    /**
     * 对应与节点列表映射的键名
     *
     * @return
     */
    String value() default "";
}
