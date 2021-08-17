package com.chinadaas.model;


import com.alibaba.fastjson.JSON;
import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.Neo4jResultParseUtils;
import com.chinadaas.commons.graph.model.RelationDto;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.DiscernAndMajorPersonEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司及主要投资者个人共同控制企业
 * @createTime 2021.08.03
 */
@Slf4j
public class DiscernAndMajorPersonModel {

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
     * 母公司节点信息
     */
    private NodeWrapper parentNode;

    /**
     * 结果状态
     */
    private ModelStatus resultStatus;

    private List<Map<String, Object>> mergeResults;

    private Map<NodeWrapper, Set<NodeWrapper>> majorInvPersonMappingMembersGroup;

    public DiscernAndMajorPersonModel(String parentId) {
        this.parentId = parentId;
        this.resultStatus = ModelStatus.NO_RESULT;
        this.nodes = Sets.newHashSet();
        this.links = Sets.newHashSet();
        this.mergeResults = Lists.newArrayList();
        this.majorInvPersonMappingMembersGroup = Maps.newHashMap();
    }

    public DiscernAndMajorPersonModel convertEntity2Model(DiscernAndMajorPersonEntity majorPersonEntity) {

        if (Objects.isNull(majorPersonEntity)) {
            return this;
        }

        if (majorPersonEntity.getGraph().emptyPath()) {
            return this;
        }

        this.resultStatus = ModelStatus.HAVE_RESULT;

        // zs: 全部图路径
        PathWrapper graph = majorPersonEntity.getGraph();
        Neo4jResultParseUtils.setFirstNode(graph.getNodeWrappers(), this.parentId);
        this.nodes.addAll(graph.getNodeWrappers());
        this.links.addAll(graph.getLinkWrappers());

        // 获得当前母公司节点信息
        this.parentNode = nodes.stream()
                .filter(node -> node.getType() == NodeType.ENT)
                .filter(NodeWrapper::isInnode)
                .findFirst()
                .orElse(null);

        // 获得主要投资人节点信息集合
        Set<NodeWrapper> majorInvPersonNodes = nodes.stream()
                .filter(node -> node.getType() == NodeType.PERSON)
                .collect(Collectors.toSet());


        // 获得当前成员节点信息
        Set<NodeWrapper> memberNodes = nodes.stream()
                .filter(node -> node.getType() == NodeType.ENT)
                .filter(node -> !node.isInnode())
                .collect(Collectors.toSet());


        this.majorInvPersonMappingMembersGroup = groupByMajorInvPerson(majorInvPersonNodes, memberNodes);

        return this;
    }

    /**
     * 合并三个节点的属性
     * zs: 不违反dry原则，代码重复但逻辑并不重复
     *
     * @return
     */
    public DiscernAndMajorPersonModel calDiscernAndMajorPersonResult() {

        this.majorInvPersonMappingMembersGroup.forEach(
                (majorInvPersonNode, memberNodes) -> {

                    for (NodeWrapper memberNode : memberNodes) {
                        // 声明一个结果项
                        Map<String, Object> mergeResult = Maps.newHashMap();

                        // 记录成员的invType
                        Object memberInvType;

                        // 成员属性处理
                        Map<String, Object> memberProperties = memberNode.getProperties();
                        memberInvType = memberProperties.remove(MemberConst.INVTYPE);
                        memberProperties.remove(MemberConst.ZSID);
                        String entStatus = (String) memberProperties.get(MemberConst.ENTSTATUS);
                        memberProperties.put(MemberConst.ENTSTATUS_DESC, AssistantUtils.getEntStatusDesc(entStatus));
                        memberProperties.put(MemberConst.ENTNAME, memberProperties.remove(MemberConst.NAME));
                        memberProperties.put(MemberConst.ENTID, memberProperties.remove(MemberConst.NODEID));
                        memberProperties.put(MemberConst.ENT_COUNTRY, memberProperties.remove(MemberConst.COUNTRY));
                        memberProperties.put(MemberConst.ENT_COUNTRY_DESC, memberProperties.remove(MemberConst.COUNTRY_DESC));
                        memberProperties.put(MemberConst.ENT_RISKINFO, memberProperties.remove(MemberConst.RISKINFO));


                        // 主要投资者属性处理
                        Map<String, Object> personProperties = majorInvPersonNode.getProperties();
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
                        mergeResult.putAll(memberProperties);

                        // 计算结果
                        mergeResult.put(MemberConst.CONPROP_PERSON2PARENT, calPerson2ParentConprop(majorInvPersonNode));
                        mergeResult.put(MemberConst.CONPROP_PERSON2SUB, calPerson2SubConprop(majorInvPersonNode, memberNode));
                        mergeResult.put(MemberConst.CONPROP_PARENT2SUB, calParent2SubConprop(memberNode));
                        mergeResult.put(MemberConst.HOLDERRTO_PERSON2PARENT, calPerson2ParentHolderrto(majorInvPersonNode));
                        mergeResult.put(MemberConst.HOLDERRTO_PERSON2SUB, calPerson2SubHolderrto(majorInvPersonNode, memberNode));
                        mergeResult.put(MemberConst.HOLDERRTO_PARENT2SUB, calParent2SubHolderrto(memberNode));

                        // 路径
                        PathWrapper pathWrapper = new PathWrapper();
                        Set<NodeWrapper> nodeWrappers = pathWrapper.getNodeWrappers();
                        Set<LinkWrapper> linkWrappers = pathWrapper.getLinkWrappers();
                        nodeWrappers.add(parentNode);
                        nodeWrappers.add(majorInvPersonNode);
                        nodeWrappers.add(memberNode);
                        linkWrappers.add(obtainParent2SubLink(memberNode));
                        linkWrappers.add(obtainPerson2SubLink(majorInvPersonNode, memberNode));
                        linkWrappers.add(obtainPerson2ParentLink(majorInvPersonNode));

                        PathWrapper filterPath = Neo4jResultParseUtils.getFilterPath(pathWrapper);
                        mergeResult.put(MemberConst.PATH, JSON.toJSON(filterPath));

                        mergeResults.add(mergeResult);
                    }

                }
        );


        return this;
    }

    public List<Map<String, Object>> getMergeResults() {
        return mergeResults;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    private LinkWrapper obtainPerson2ParentLink(NodeWrapper majorInvPersonNode) {
        return links.stream()
                .filter(link -> link.getFrom() == majorInvPersonNode.getId())
                .filter(link -> link.getTo() == parentNode.getId())
                .findFirst()
                .orElse(null);
    }

    private LinkWrapper obtainPerson2SubLink(NodeWrapper majorInvPersonNode, NodeWrapper memberNode) {
        return links.stream()
                .filter(link -> link.getFrom() == majorInvPersonNode.getId())
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
     * 主要投资人员到成员公司的出资比例
     *
     * @param majorInvPersonNode
     * @param memberNode
     * @return
     */
    private String calPerson2SubConprop(NodeWrapper majorInvPersonNode,
                                        NodeWrapper memberNode) {

        LinkWrapper person2SubLink = links.stream()
                .filter(link -> link.getFrom() == majorInvPersonNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getConprop(person2SubLink);
    }

    /**
     * 主要投资人员到成员公司的持股比例
     *
     * @param majorInvPersonNode
     * @param memberNode
     * @return
     */
    private String calPerson2SubHolderrto(NodeWrapper majorInvPersonNode,
                                          NodeWrapper memberNode) {

        LinkWrapper person2SubLink = links.stream()
                .filter(link -> link.getFrom() == majorInvPersonNode.getId())
                .filter(link -> link.getTo() == memberNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getHolderrto(person2SubLink);
    }

    /**
     * 主要投资人员到母公司的出资比例
     *
     * @param majorInvPersonNode
     * @return
     */
    private String calPerson2ParentConprop(NodeWrapper majorInvPersonNode) {
        LinkWrapper person2ParentLink = links.stream()
                .filter(link -> link.getFrom() == majorInvPersonNode.getId())
                .filter(link -> link.getTo() == parentNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getConprop(person2ParentLink);
    }

    /**
     * 主要投资人员到母公司的持股比例
     *
     * @param majorInvPersonNode
     * @return
     */
    private String calPerson2ParentHolderrto(NodeWrapper majorInvPersonNode) {
        LinkWrapper person2ParentLink = links.stream()
                .filter(link -> link.getFrom() == majorInvPersonNode.getId())
                .filter(link -> link.getTo() == parentNode.getId())
                .findFirst()
                .orElse(null);

        return AssistantUtils.getHolderrto(person2ParentLink);
    }

    private Map<NodeWrapper, Set<NodeWrapper>> groupByMajorInvPerson(Set<NodeWrapper> majorInvPersonNodes,
                                                                     Set<NodeWrapper> memberNodes) {

        Map<NodeWrapper, Set<NodeWrapper>> resultMap = Maps.newHashMap();

        for (NodeWrapper personNode : majorInvPersonNodes) {
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
}
