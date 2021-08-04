package com.chinadaas.component.mapper.handler.impl;

import com.chinadaas.common.util.Neo4jResultParseUtils;
import com.chinadaas.component.mapper.annotation.NodesType;
import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.FieldPopulateException;
import com.chinadaas.component.mapper.handler.AbstractAnnotationHandler;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.types.Node;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 节点集合注解处理器
 * @createTime 2021.07.02
 */
@Slf4j
@Component
public class NodesTypeHandler extends AbstractAnnotationHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HandlerContext handlerContext) {
        Field field = handlerContext.getField();
        field.setAccessible(true);
        Map<String, Object> properties = handlerContext.getProperties();
        Object t = handlerContext.getInstance();

        // zs: 处理neo4j中的节点集合
        NodesType nodesType = field.getAnnotation(NodesType.class);
        List<Node> nodes = (List<Node>) properties.get(nodesType.value());
        Set<NodeWrapper> nodeWrappers = Sets.newHashSet();
        for (Node node : nodes) {
            NodeWrapper nodeWrapper = Neo4jResultParseUtils.parseNode(node);
            nodeWrappers.add(nodeWrapper);
        }

        try {
            field.set(t, nodeWrappers);
        } catch (IllegalAccessException e) {
            log.error("field set fail, reason: ");
            throw new FieldPopulateException(e);
        }
    }

    @Override
    protected boolean doAnnotationHit(Field field) {
        return field.isAnnotationPresent(NodesType.class);
    }
}
