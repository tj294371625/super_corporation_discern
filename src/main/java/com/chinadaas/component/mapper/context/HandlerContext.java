package com.chinadaas.component.mapper.context;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author liubc
 * @version 1.0.0
 * @description 注解处理器上下文
 * @createTime 2021.07.01
 */
@Getter
public class HandlerContext {

    /**
     * 实例
     */
    private final Object instance;

    /**
     * 实例中的成员字段
     */
    private final Field field;

    /**
     * neo4j查询结果
     */
    private final Map<String, Object> properties;

    public HandlerContext(Object instance, Field field, Map<String, Object> properties) {
        this.instance = instance;
        this.field = field;
        this.properties = properties;
    }
}
