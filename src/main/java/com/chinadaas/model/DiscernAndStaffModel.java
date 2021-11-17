package com.chinadaas.model;

import com.alibaba.fastjson.JSON;
import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.Neo4jResultParseUtils;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.DiscernAndStaffEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司及其关键管理人员共同控制的公司
 * @createTime 2021.08.03
 */
public class DiscernAndStaffModel {

    /**
     * 母公司id
     */
    private String parentId;

    /**
     * 全部节点
     */
    private Set<NodeWrapper> nodes;

    /**
     * 全部关系
     */
    private Set<LinkWrapper> links;

    /**
     * 母公司节点
     */
    private NodeWrapper parentNode;

    private List<Map<String, Object>> mergeResults;

    private ModelStatus resultStatus;

    private Map<NodeWrapper, Set<NodeWrapper>> staffPersonMappingMembersGroup;

    public DiscernAndStaffModel(String parentId) {
        this.parentId = parentId;
        this.nodes = Sets.newHashSet();
        this.links = Sets.newHashSet();
        this.mergeResults = Lists.newArrayList();
        this.resultStatus = ModelStatus.NO_RESULT;
        this.staffPersonMappingMembersGroup = Maps.newHashMap();
    }

    public DiscernAndStaffModel convertEntity2Model(DiscernAndStaffEntity entity) {

        if (Objects.isNull(entity)) {
            return this;
        }

        if (entity.getGraph().emptyPath()) {
            return this;
        }

        this.resultStatus = ModelStatus.HAVE_RESULT;

        PathWrapper graph = entity.getGraph();
        Neo4jResultParseUtils.setFirstNode(graph.getNodeWrappers(), this.parentId);
        this.nodes.addAll(graph.getNodeWrappers());
        this.links.addAll(graph.getLinkWrappers());

        // 获得当前母公司节点信息
        this.parentNode = nodes.stream()
                .filter(node -> node.getType() == NodeType.ENT)
                .filter(NodeWrapper::isInnode)
                .findFirst()
                .orElse(null);

        // 获得关键管理人员节点信息集合
        Set<NodeWrapper> staffPersonNodes = nodes.stream()
                .filter(node -> node.getType() == NodeType.PERSON)
                .collect(Collectors.toSet());
        for (NodeWrapper staffPersonNode : staffPersonNodes) {
            String staffPersonId = staffPersonNode.obtainEntId();
            Neo4jResultParseUtils.setFirstNode(graph.getNodeWrappers(), staffPersonId);
        }

        // 获得当前成员节点信息集合
        Set<NodeWrapper> memberNodes = nodes.stream()
                .filter(node -> node.getType() == NodeType.ENT)
                .filter(node -> !node.isInnode())
                .collect(Collectors.toSet());

        this.staffPersonMappingMembersGroup = groupByStaffPerson(staffPersonNodes, memberNodes);

        return this;
    }

    private Map<NodeWrapper, Set<NodeWrapper>> groupByStaffPerson(Set<NodeWrapper> staffPersonNodes,
                                                                  Set<NodeWrapper> memberNodes) {

        Map<NodeWrapper, Set<NodeWrapper>> resultMap = Maps.newHashMap();

        for (NodeWrapper personNode : staffPersonNodes) {
            Set<LinkWrapper> personInvMemberLinks = links.stream()
                    .filter(link -> link.getFrom() == personNode.getId())
                    .filter(link -> link.getTo() != parentNode.getId())
                    .collect(Collectors.toSet());

            Set<NodeWrapper> nodeWrappers = Sets.newHashSet();
            for (LinkWrapper link : personInvMemberLinks) {
                NodeWrapper memberNode = memberNodes.stream()
                        .filter(node -> link.getTo() == node.getId())
                        .findFirst()
                        .orElse(null);

                nodeWrappers.add(memberNode);
            }

            resultMap.put(personNode, nodeWrappers);
        }

        return resultMap;
    }

    /**
     * 合并三个节点的属性
     * zs: 不违反dry原则，代码重复但逻辑并不重复
     *
     * @return
     */
    public DiscernAndStaffModel calDiscernAndStaffResult() {

        this.staffPersonMappingMembersGroup.forEach(
                (staffPersonNode, memberNodes) -> {

                    for (NodeWrapper memberNode : memberNodes) {
                        // 声明一个结果项
                        Map<String, Object> mergeResult = Maps.newHashMap();

                        // 记录成员的invType
                        Object memberInvType;

                        // 成员属性处理
                        NodeWrapper memberDeepCopy = JSON.parseObject(JSON.toJSONString(memberNode), NodeWrapper.class);
                        Map<String, Object> memberProperties = memberDeepCopy.getProperties();

                        // zs: 浅拷贝问题
                        Map<String, Object> copyMemberProperties = JSON.parseObject(JSON.toJSONString(memberProperties), Map.class);

                        memberProperties.put(MemberConst.ENTID, memberProperties.remove(MemberConst.NODEID));

                        memberInvType = copyMemberProperties.remove(MemberConst.INVTYPE);
                        copyMemberProperties.remove(MemberConst.ZSID);
                        String entStatus = (String) copyMemberProperties.get(MemberConst.ENTSTATUS);
                        copyMemberProperties.put(MemberConst.ENTSTATUS_DESC, AssistantUtils.getEntStatusDesc(entStatus));
                        copyMemberProperties.put(MemberConst.ENTNAME, copyMemberProperties.remove(MemberConst.NAME));
                        copyMemberProperties.put(MemberConst.ENTID, copyMemberProperties.remove(MemberConst.NODEID));
                        copyMemberProperties.put(MemberConst.ENT_COUNTRY, copyMemberProperties.remove(MemberConst.COUNTRY));
                        copyMemberProperties.put(MemberConst.ENT_COUNTRY_DESC, copyMemberProperties.remove(MemberConst.COUNTRY_DESC));
                        copyMemberProperties.put(MemberConst.ENT_RISKINFO, copyMemberProperties.remove(MemberConst.RISKINFO));


                        // 主要投资者属性处理
                        NodeWrapper staffPersonDeepCopy = JSON.parseObject(JSON.toJSONString(staffPersonNode), NodeWrapper.class);
                        Map<String, Object> personProperties = staffPersonDeepCopy.getProperties();
                        personProperties.remove(MemberConst.INVTYPE);
                        personProperties.remove(MemberConst.NODEID);
                        AssistantUtils.generateZspId(personProperties, this.parentId);
                        personProperties.put(MemberConst.PERSON_RISKINFO, personProperties.remove(MemberConst.RISKINFO));
                        personProperties.put(MemberConst.PERSON_COUNTRY, personProperties.remove(MemberConst.COUNTRY));
                        personProperties.put(MemberConst.PERSON_COUNTRY_DESC, personProperties.remove(MemberConst.COUNTRY_DESC));


                        String id = UUID.randomUUID().toString().replaceAll("-", "");
                        mergeResult.put(MemberConst._ID, id);
                        mergeResult.put(MemberConst.RELATION_DENSITY, "半紧密层");
                        mergeResult.put(MemberConst.RELATION, "直接");
                        mergeResult.put(MemberConst.PARENT_ID, this.parentId);
                        mergeResult.put(MemberConst.INVTYPE, memberInvType);
                        mergeResult.putAll(personProperties);
                        mergeResult.putAll(copyMemberProperties);

                        // 计算结果
                        mergeResult.put(MemberConst.POSITION, obtainPosition(staffPersonDeepCopy));
                        mergeResult.put(MemberConst.POSITION_DESC, obtainPositionDesc(staffPersonDeepCopy));
                        mergeResult.put(MemberConst.CONPROP_PERSON2SUB, calPerson2SubConprop(staffPersonDeepCopy, memberDeepCopy));
                        mergeResult.put(MemberConst.CONPROP_PARENT2SUB, calParent2SubConprop(memberDeepCopy));
                        mergeResult.put(MemberConst.HOLDERRTO_PERSON2SUB, calPerson2SubHolderrto(staffPersonDeepCopy, memberDeepCopy));
                        mergeResult.put(MemberConst.HOLDERRTO_PARENT2SUB, calParent2SubHolderrto(memberDeepCopy));

                        // 路径
                        PathWrapper pathWrapper = new PathWrapper();
                        Set<NodeWrapper> nodeWrappers = pathWrapper.getNodeWrappers();
                        Set<LinkWrapper> linkWrappers = pathWrapper.getLinkWrappers();

                        // zs: 过滤
                        NodeWrapper parentDeepCopy = JSON.parseObject(JSON.toJSONString(parentNode), NodeWrapper.class);
                        Map<String, Object> parentProperties = parentDeepCopy.getProperties();
                        parentProperties.put(MemberConst.ENTID, parentProperties.remove(MemberConst.NODEID));
                        nodeWrappers.add(parentDeepCopy);
                        nodeWrappers.add(staffPersonDeepCopy);
                        nodeWrappers.add(memberDeepCopy);
                        linkWrappers.add(obtainParent2SubLink(memberDeepCopy));
                        linkWrappers.add(obtainPerson2SubLink(staffPersonDeepCopy, memberDeepCopy));
                        // zs: 任职关系有多条边
                        linkWrappers.addAll(obtainPerson2ParentLink(staffPersonDeepCopy));

                        PathWrapper filterPath = Neo4jResultParseUtils.getFilterPath(pathWrapper);
                        mergeResult.put(MemberConst.PATH, JSON.toJSON(filterPath));

                        mergeResults.add(mergeResult);
                    }

                }
        );

        return this;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    public List<Map<String, Object>> getMergeResults() {
        return mergeResults;
    }

    private String obtainPositionDesc(NodeWrapper staffPersonNode) {
        Set<LinkWrapper> positionLinks = obtainPerson2ParentLink(staffPersonNode);

        Set<String> positionDescs = positionLinks.stream()
                .map(link -> (String) link.getProperties().get(MemberConst.POSITION_DESC))
                .collect(Collectors.toSet());

        return JSON.toJSONString(positionDescs);
    }

    private String obtainPosition(NodeWrapper staffPersonNode) {
        Set<LinkWrapper> positionLinks = obtainPerson2ParentLink(staffPersonNode);

        Set<String> positions = positionLinks.stream()
                .map(link -> (String) link.getProperties().get(MemberConst.POSITION))
                .collect(Collectors.toSet());

        return JSON.toJSONString(positions);
    }

    private Set<LinkWrapper> obtainPerson2ParentLink(NodeWrapper staffPersonNode) {
        return links.stream()
                .filter(link -> link.getFrom() == staffPersonNode.getId())
                .filter(link -> link.getTo() == parentNode.getId())
                .collect(Collectors.toSet());
    }

    private LinkWrapper obtainPerson2SubLink(NodeWrapper staffPersonNode, NodeWrapper memberNode) {
        return links.stream()
                .filter(link -> link.getFrom() == staffPersonNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);
    }

    private LinkWrapper obtainParent2SubLink(NodeWrapper memberNode) {
        return links.stream()
                .filter(link -> link.getFrom() == parentNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);
    }

    /**
     * 母公司到成员公司的出资比例
     *
     * @param memberNode
     * @return
     */
    private String calParent2SubConprop(NodeWrapper memberNode) {
        // 获取母公司到成员公司的投资边
        LinkWrapper parent2SubLink = links.stream()
                .filter(link -> link.getFrom() == parentNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getConprop(parent2SubLink);
    }

    /**
     * 母公司到成员公司的持股比例
     *
     * @param memberNode
     * @return
     */
    private String calParent2SubHolderrto(NodeWrapper memberNode) {
        // 获取母公司到成员公司的投资边
        LinkWrapper parent2SubLink = links.stream()
                .filter(link -> link.getFrom() == parentNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getHolderrto(parent2SubLink);
    }

    /**
     * 主要任职人员到成员公司的出资比例
     *
     * @param staffPersonNode
     * @param memberNode
     * @return
     */
    private String calPerson2SubConprop(NodeWrapper staffPersonNode,
                                        NodeWrapper memberNode) {

        LinkWrapper person2SubLink = links.stream()
                .filter(link -> link.getFrom() == staffPersonNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getConprop(person2SubLink);
    }

    /**
     * 主要任职人员到成员公司的持股比例
     *
     * @param staffPersonNode
     * @param memberNode
     * @return
     */
    private String calPerson2SubHolderrto(NodeWrapper staffPersonNode,
                                          NodeWrapper memberNode) {

        LinkWrapper person2SubLink = links.stream()
                .filter(link -> link.getFrom() == staffPersonNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getHolderrto(person2SubLink);
    }

}