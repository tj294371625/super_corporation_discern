package com.chinadaas.component.mapper.handler;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author lawliet
 * @version 1.0.0
 * @Description 注解处理器模板
 * @createTime 2021.07.01
 */
public abstract class AbstractAnnotationHandler implements AnnotationHandler {

    @Override
    public boolean annotationHit(Field field) {
        if (Objects.isNull(field)) {
            return false;
        }

        field.setAccessible(true);

        // subclass handle
        return doAnnotationHit(field);
    }

    /**
     * 注解命中
     *
     * @param field 实例成员字段
     * @return
     */
    protected abstract boolean doAnnotationHit(Field field);
}
