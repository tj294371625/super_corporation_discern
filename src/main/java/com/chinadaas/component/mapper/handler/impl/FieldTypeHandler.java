package com.chinadaas.component.mapper.handler.impl;

import com.chinadaas.component.mapper.annotation.FieldType;
import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.FieldPopulateException;
import com.chinadaas.component.mapper.handler.AbstractAnnotationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 字段注解处理器
 * @createTime 2021.07.02
 */
@Slf4j
@Component
public class FieldTypeHandler extends AbstractAnnotationHandler {

    @Override
    public void handle(HandlerContext handlerContext) {
        Field field = handlerContext.getField();
        Map<String, Object> properties = handlerContext.getProperties();
        Object t = handlerContext.getInstance();

        // zs: 处理neo4j结果中普通字段
        FieldType fieldType = field.getAnnotation(FieldType.class);
        Object value = properties.get(fieldType.value());

        try {
            field.set(t, value);
        } catch (IllegalAccessException e) {
            log.error("field set fail, reason: ");
            throw new FieldPopulateException(e);
        }
    }

    @Override
    protected boolean doAnnotationHit(Field field) {
        return field.isAnnotationPresent(FieldType.class);
    }

}
