package com.chinadaas.component.mapper.handler;

import com.chinadaas.component.mapper.context.HandlerContext;

import java.lang.reflect.Field;

/**
 * @author lawliet
 * @version 1.0.0
 * @Description 注解处理定义
 * @createTime 2021.07.01
 */
public interface AnnotationHandler {

    /**
     * 装配处理
     *
     * @param handlerContext 注解处理上下文
     */
    void handle(HandlerContext handlerContext);

    /**
     * 根据注解类型命中相应注解类型的处理器
     *
     * @param field 实例成员字段
     * @return
     */
    boolean annotationHit(Field field);
}
