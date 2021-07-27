package com.chinadaas.component.mapper.handler.impl;

import com.chinadaas.common.utils.Neo4jResultParseUtils;
import com.chinadaas.component.mapper.annotation.LinksType;
import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.FieldPopulateException;
import com.chinadaas.component.mapper.handler.AbstractAnnotationHandler;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liubc
 * @version 1.0.0
 * @description 边集合注解处理器
 * @createTime 2021.07.02
 */
@Slf4j
@Component
public class LinksTypeHandler extends AbstractAnnotationHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HandlerContext handlerContext) {
        Field field = handlerContext.getField();
        Map<String, Object> properties = handlerContext.getProperties();
        Object t = handlerContext.getInstance();

        // zs: 处理neo4j中的边集合
        LinksType linksType = field.getAnnotation(LinksType.class);
        List<Relationship> links = (List<Relationship>) properties.get(linksType.value());
        Set<LinkWrapper> linkWrappers = Sets.newHashSet();
        for (Relationship relationship : links) {
            LinkWrapper linkWrapper = Neo4jResultParseUtils.parseRelation(relationship);
            linkWrappers.add(linkWrapper);
        }

        try {
            field.set(t, linkWrappers);
        } catch (IllegalAccessException e) {
            log.error("field set fail, reason: ");
            throw new FieldPopulateException(e);
        }
    }

    @Override
    protected boolean doAnnotationHit(Field field) {
        return field.isAnnotationPresent(LinksType.class);
    }
}
