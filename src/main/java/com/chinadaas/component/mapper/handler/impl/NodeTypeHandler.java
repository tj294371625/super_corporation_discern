package com.chinadaas.component.mapper.handler.impl;

import com.chinadaas.common.utils.Neo4jResultParseUtils;
import com.chinadaas.component.mapper.annotation.NodeType;
import com.chinadaas.component.mapper.context.HandlerContext;
import com.chinadaas.component.mapper.exception.FieldPopulateException;
import com.chinadaas.component.mapper.handler.AbstractAnnotationHandler;
import com.chinadaas.component.wrapper.NodeWrapper;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.types.Node;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author liubc
 * @version 1.0.0
 * @Description 节点注解处理
 * @createTime 2021.07.01
 */
@Slf4j
@Component
public class NodeTypeHandler extends AbstractAnnotationHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HandlerContext handlerContext) {
        Field field = handlerContext.getField();
        Map<String, Object> properties = handlerContext.getProperties();
        Object t = handlerContext.getInstance();

        // zs: 处理neo4j中的节点
        NodeType nodeType = field.getAnnotation(NodeType.class);
        Node node = (Node) properties.get(nodeType.value());
        NodeWrapper nodeWrapper = Neo4jResultParseUtils.parseNode(node);

        try {
            field.set(t, nodeWrapper);
        } catch (IllegalAccessException e) {
            log.error("field set fail, reason: ");
            throw new FieldPopulateException(e);
        }
    }

    @Override
    protected boolean doAnnotationHit(Field field) {
        return field.isAnnotationPresent(NodeType.class);
    }
}
