package com.chinadaas.common.utils;

import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.BeanInstantiationException;
import com.chinadaas.component.mapper.handler.AnnotationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author liubc
 * @version 1.0.0
 * @description Bean创建工具
 * @createTime 2021.07.03
 */
@Slf4j
public abstract class BeanUtils {

    /**
     * 创建一个Bean实例
     *
     * @param classType  转换实体的Class类型
     * @param properties neo4j查询结果
     * @param handlers   注解处理集
     * @param <T>        要转换实体的泛型
     * @return
     */
    public static <T> T createBean(Class<T> classType,
                                   Map<String, Object> properties,
                                   List<AnnotationHandler> handlers) {
        T t;
        try {
            t = classType.newInstance();

            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                HandlerContext context = new HandlerContext(t, field, properties);

                for (AnnotationHandler handler : handlers) {
                    if (handler.annotationHit(field)) {
                        handler.handle(context);
                    }
                }

            }

        } catch (InstantiationException | IllegalAccessException e) {
            log.error("bean create fail, error stack: ");
            throw new BeanInstantiationException(e);
        }

        return t;
    }
}
