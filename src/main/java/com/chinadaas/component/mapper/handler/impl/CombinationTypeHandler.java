package com.chinadaas.component.mapper.handler.impl;

import com.chinadaas.common.utils.BeanUtils;
import com.chinadaas.common.utils.Neo4jResultParseUtils;
import com.chinadaas.component.mapper.annotation.CombinationType;
import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.FieldPopulateException;
import com.chinadaas.component.mapper.handler.AbstractAnnotationHandler;
import com.chinadaas.component.mapper.handler.AnnotationHandler;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lawliet
 * @version 1.0.0
 * @Description 组合注解处理器
 * @createTime 2021.07.01
 */
@Slf4j
@Component
public class CombinationTypeHandler extends AbstractAnnotationHandler {

    private final List<AnnotationHandler> handlers;

    @Autowired
    public CombinationTypeHandler(List<AnnotationHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HandlerContext handlerContext) {
        Field field = handlerContext.getField();
        Map<String, Object> properties = handlerContext.getProperties();
        Object t = handlerContext.getInstance();

        // zs: 处理neo4j中 点或边 与组合实体的映射
        CombinationType combinationType = field.getAnnotation(CombinationType.class);
        Map<String, Object> combProperties = Collections.EMPTY_MAP;
        Object unKnown = properties.get(combinationType.value());
        if (unKnown instanceof Node) {
            NodeWrapper nodeWrapper = Neo4jResultParseUtils.parseNode((Node) unKnown);
            combProperties = Maps.newHashMap(nodeWrapper.getProperties());
        }
        if (unKnown instanceof Relationship) {
            LinkWrapper linkWrapper = Neo4jResultParseUtils.parseRelation((Relationship) unKnown);
            combProperties = Maps.newHashMap(linkWrapper.getProperties());
        }
        if (unKnown instanceof Map) {
            combProperties = Maps.newHashMap((Map<String, Object>) unKnown);
        }
        Class<?> combClassType = field.getType();

        Object combObj = handleInternalField(combProperties, combClassType);

        try {
            field.set(t, combObj);
        } catch (IllegalAccessException e) {
            log.error("field set fail, reason: ");
            throw new FieldPopulateException(e);
        }
    }

    @Override
    protected boolean doAnnotationHit(Field field) {
        return field.isAnnotationPresent(CombinationType.class);
    }

    /**
     * 处理组合对象内部字段
     *
     * @param combProperties
     * @param combClassType
     * @return
     */
    private Object handleInternalField(Map<String, Object> combProperties, Class<?> combClassType) {

        if (CollectionUtils.isEmpty(combProperties)
                || Objects.isNull(combClassType)) {
            return null;
        }

        return BeanUtils.createBean(combClassType, combProperties, handlers);
    }
}
