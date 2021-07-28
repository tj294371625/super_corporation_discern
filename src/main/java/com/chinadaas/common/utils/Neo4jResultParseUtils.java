package com.chinadaas.common.utils;


import com.chinadaas.commons.type.NodeType;
import com.chinadaas.commons.type.RelationType;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.TwoNodesEntity;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author lawliet
 * @version 1.0.0
 * @description neo4j结果解析
 * @createTime 2021.07.01
 */
public abstract class Neo4jResultParseUtils {

    private static final String NAME_KEY = "name";
    private static final String LEGAL_KEY = "legal";
    private static final String RISK_SCORE = "riskscore";
    private static final String RELATION_ID = "relationid";

    public static LinkWrapper parseRelation(Relationship relationship) {

        if (relationship != null) {
            long endNode = relationship.endNodeId();
            long startNode = relationship.startNodeId();
            long id = relationship.id();
            String type = relationship.type();
            int relationType = RelationType.parseRelationType(type);
            Map<String, Object> properties = relationship.asMap();
            Map<String, Object> mutableProperties = new HashMap<>(properties.size());
            mutableProperties.putAll(properties);

            if (LEGAL_KEY.equals(type)) {
                mutableProperties.put(LEGAL_KEY, true);
            }

            mutableProperties.remove(RISK_SCORE);
            mutableProperties.remove(RELATION_ID);
            LinkWrapper linkWrapper = new LinkWrapper();
            linkWrapper.setId(id);
            linkWrapper.setFrom(startNode);
            linkWrapper.setTo(endNode);
            linkWrapper.setProperties(mutableProperties);
            linkWrapper.setType(relationType);
            return linkWrapper;
        }
        return null;
    }

    public static NodeWrapper parseNode(Node node) {

        if (node != null) {
            Map<String, Object> properties = node.asMap();
            Map<String, Object> mutableProperties = new HashMap<>(properties.size());
            mutableProperties.putAll(properties);
            String name = (String) properties.get(NAME_KEY);
            String lable = node.labels().iterator().next();
            long id = node.id();

            NodeWrapper nodeWrapper = new NodeWrapper();
            nodeWrapper.setId(id);
            nodeWrapper.setName(name);
            nodeWrapper.setType(NodeType.parseNodeType(lable));
            nodeWrapper.setProperties(mutableProperties);
            return nodeWrapper;
        }
        return null;
    }

    public static NodeWrapper obtainSpecialNode(String nodeId, List<TwoNodesEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }

        TwoNodesEntity twoNodesEntity = entityList.get(0);
        PathWrapper twoNodesPath = twoNodesEntity.getTwoNodesPath();
        Set<NodeWrapper> nodeWrappers = twoNodesPath.getNodeWrappers();

        return nodeWrappers.stream()
                .filter(nodeWrapper -> Objects.equals(nodeId, nodeWrapper.getEntId()))
                .findFirst()
                .orElse(null);

    }

}
