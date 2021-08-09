package com.chinadaas.common.util;


import com.chinadaas.commons.type.NodeType;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.TwoNodesEntity;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.beans.BeanUtils;
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
    private static final Set<String> NODE_PROPERTIES;

    static {
        NODE_PROPERTIES = Sets.newHashSet();
        NODE_PROPERTIES.add("name");
        NODE_PROPERTIES.add("creditcode");
        NODE_PROPERTIES.add("regno");
        NODE_PROPERTIES.add("entid");
    }

    public static LinkWrapper parseRelation(Relationship relationship) {

        if (relationship != null) {
            long endNode = relationship.endNodeId();
            long startNode = relationship.startNodeId();
            long id = relationship.id();
            String type = relationship.type();
            int relationType = AssistantUtils.parseRelationType(type);
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

    /**
     * 标记起始点
     *
     * @param nodes
     * @param firstNodeId
     */
    public static void setFirstNode(Set<NodeWrapper> nodes, String firstNodeId) {

        if (!CollectionUtils.isEmpty(nodes) && StringUtils.isNotBlank(firstNodeId)) {
            for (NodeWrapper node : nodes) {
                if (Objects.equals(firstNodeId, node.obtainEntId())) {
                    node.setInnode(true);
                }
            }
        }

    }

    public static NodeWrapper obtainSpecialNode(String nodeId, List<TwoNodesEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }

        TwoNodesEntity twoNodesEntity = entityList.get(0);
        PathWrapper twoNodesPath = twoNodesEntity.getTwoNodesPath();
        Set<NodeWrapper> nodeWrappers = twoNodesPath.getNodeWrappers();

        return nodeWrappers.stream()
                .filter(
                        nodeWrapper -> Objects.equals(nodeId, nodeWrapper.obtainEntId())
                                || Objects.equals(nodeId, nodeWrapper.obtainZsId())
                )
                .findFirst()
                .orElse(null);

    }

    public static PathWrapper getFilterPath(PathWrapper path) {

        if (Objects.nonNull(path)) {
            PathWrapper filterPath = new PathWrapper();
            Set<NodeWrapper> nodeWrappers = path.getNodeWrappers();
            Set<NodeWrapper> filterNodes = new HashSet<>(nodeWrappers.size());
            nodeWrappers.forEach(nodeWrapper -> {
                NodeWrapper filterNode = new NodeWrapper();

                BeanUtils.copyProperties(nodeWrapper, filterNode);
                Map<String, Object> filterNodeProperties = filterNode.getProperties();

                if (!CollectionUtils.isEmpty(filterNodeProperties)) {
                    Set<Map.Entry<String, Object>> entries = filterNodeProperties.entrySet();
                    Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> next = iterator.next();
                        String key = next.getKey();
                        if (!NODE_PROPERTIES.contains(key)) {
                            iterator.remove();
                        }
                    }
                }

                filterNodes.add(filterNode);
            });

            filterPath.setNodeWrappers(filterNodes);
            filterPath.setLinkWrappers(path.getLinkWrappers());
            return filterPath;
        }

        return null;
    }

}