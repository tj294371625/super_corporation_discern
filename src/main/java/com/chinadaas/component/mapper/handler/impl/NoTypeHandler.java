package com.chinadaas.component.mapper.handler.impl;

import com.chinadaas.component.mapper.annotation.*;
import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.FieldPopulateException;
import com.chinadaas.component.mapper.handler.AbstractAnnotationHandler;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author liubc
 * @version 1.0.0
 * @description 无注解处理器
 * @createTime 2021.07.02
 */
@Slf4j
@Component
public class NoTypeHandler extends AbstractAnnotationHandler {

    private List<Class<? extends Annotation>> annotationClasses;

    @PostConstruct
    public void init() {
        annotationClasses = Lists.newArrayList();
        annotationClasses.add(NodeType.class);
        annotationClasses.add(NodesType.class);
        annotationClasses.add(LinkType.class);
        annotationClasses.add(LinksType.class);
        annotationClasses.add(FieldType.class);
        annotationClasses.add(CombinationType.class);
    }

    @Override
    public void handle(HandlerContext handlerContext) {
        Field field = handlerContext.getField();
        Map<String, Object> properties = handlerContext.getProperties();
        Object t = handlerContext.getInstance();

        // zs: 无注解字段处理
        Object value = properties.get(field.getName());

        try {
            field.set(t, value);
        } catch (IllegalAccessException e) {
            log.error("field set fail, reason: ");
            throw new FieldPopulateException(e);
        }
    }

    @Override
    protected boolean doAnnotationHit(Field field) {
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            if (field.isAnnotationPresent(annotationClass)) {
                return false;
            }
        }
        return true;
    }
}
