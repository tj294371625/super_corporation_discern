package com.chinadaas.component.mapper.handler.impl;

import com.chinadaas.common.utils.Neo4jResultParseUtils;
import com.chinadaas.component.mapper.annotation.LinkType;
import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.FieldPopulateException;
import com.chinadaas.component.mapper.handler.AbstractAnnotationHandler;
import com.chinadaas.component.wrapper.LinkWrapper;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author liubc
 * @version 1.0.0
 * @description 边注解处理器
 * @createTime 2021.07.02
 */
@Slf4j
@Component
public class LinkTypeHandler extends AbstractAnnotationHandler {

    @Override
    public void handle(HandlerContext handlerContext) {
        Field field = handlerContext.getField();
        Map<String, Object> properties = handlerContext.getProperties();
        Object t = handlerContext.getInstance();

        // zs: 处理neo4j中的边
        LinkType linkType = field.getAnnotation(LinkType.class);
        Relationship relationship = (Relationship) properties.get(linkType.value());
        LinkWrapper linkWrapper = Neo4jResultParseUtils.parseRelation(relationship);

        try {
            field.set(t, linkWrapper);
        } catch (IllegalAccessException e) {
            log.error("field set fail, reason: ");
            throw new FieldPopulateException(e);
        }
    }

    @Override
    protected boolean doAnnotationHit(Field field) {
        return field.isAnnotationPresent(LinkType.class);
    }
}
