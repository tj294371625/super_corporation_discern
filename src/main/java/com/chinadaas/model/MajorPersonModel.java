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
import com.chinadaas.entity.MajorPersonEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司主要投资者个人直接对外控制公司模型
 * @createTime 2021.08.05
 */
public class MajorPersonModel {

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
     * 主要投资人节点集合
     */
    Set<NodeWrapper> majorInvPersonNodes;

    /**
     * 母公司节点信息
     */
    private NodeWrapper parentNode;

    private List<Map<String, Object>> majorControlEnts;

    private ModelStatus resultStatus;

    public MajorPersonModel(String parentId) {
        this.parentId = parentId;
        this.nodes = Sets.newHashSet();
        this.links = Sets.newHashSet();
        this.majorControlEnts = Lists.newArrayList();
        this.resultStatus = ModelStatus.NO_RESULT;
    }

    public MajorPersonModel convertEntity2Model(MajorPersonEntity majorPersonEntity) {
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
        this.majorInvPersonNodes = nodes.stream()
                .filter(node -> node.getType() == NodeType.PERSON)
                .collect(Collectors.toSet());

        return this;
    }

    public MajorPersonModel calMajorPersonModelResult(BiFunction<String, String, List<Map>> commonPersonControlEnt) {

        for (NodeWrapper personNode : this.majorInvPersonNodes) {
            String zsId = personNode.obtainZsId();

            List<Map> tempResultList = commonPersonControlEnt.apply(zsId, parentId);
            if (CollectionUtils.isEmpty(tempResultList)) {
                continue;
            }

            LinkWrapper personToParentLink = links.stream()
                    .filter(link -> link.getFrom() == personNode.getId())
                    .filter(link -> link.getTo() == parentNode.getId())
                    .findFirst()
                    .orElse(null);

            // 获取投资人控制的母公司
            List<Map> controlParents = tempResultList.stream()
                    .filter(ent -> Objects.nonNull(ent.get(MemberConst.PARENT_ID)))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(controlParents)) {
                for (Map controlParent : controlParents) {
                    majorControlEnts.add(processEntInfo(controlParent, EntType.PARENT, personNode, personToParentLink));
                }
            }

            // 获取投资人控制的成员公司
            List<Map> controlMembers = tempResultList.stream()
                    .filter(ent -> Objects.isNull(ent.get(MemberConst.PARENT_ID)))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(controlMembers)) {
                for (Map controlMember : controlMembers) {
                    majorControlEnts.add(processEntInfo(controlMember, EntType.MEMBER, personNode, personToParentLink));
                }
            }

        }

        // zs： 对结果去重
        this.majorControlEnts = majorControlEnts.stream()
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toCollection(
                                        () -> new TreeSet<>(Comparator.comparing(m -> m.get("entid").toString()))
                                ),
                                ArrayList::new
                        )
                );

        return this;
    }

    private Map<String, Object> processEntInfo(Map entInfo, EntType type, NodeWrapper personNode, LinkWrapper personToParentLink) {
        Map<String, Object> tempResult = Maps.newHashMap();

        Map entProperties;
        if (EntType.PARENT.equals(type)) {
            entProperties = (Map) entInfo.get(MemberConst.PARENT_PROPERTY);
            tempResult.put(MemberConst.PATH, entInfo.get(MemberConst.CTRL2PARENT_PATH));
            tempResult.put(MemberConst.FINAL_CGZB, entInfo.get(MemberConst.CTRL2PARENT_CGZB));
        } else {
            entProperties = (Map) entInfo.get(MemberConst.SOURCE_PROPERTY);
            tempResult.put(MemberConst.PATH, entInfo.get(MemberConst.CTRL2SOURCE_PATH));
            tempResult.put(MemberConst.FINAL_CGZB, entInfo.get(MemberConst.CTRL2SOURCE_CGZB));
        }

        // 记录企业的invType
        Object entInvType;

        // 企业属性处理
        entInvType = entProperties.remove(MemberConst.INVTYPE);
        entProperties.remove(MemberConst.ZSID);
        String entStatus = (String) entProperties.get(MemberConst.ENTSTATUS);
        entProperties.put(MemberConst.ENTSTATUS_DESC, AssistantUtils.getEntStatusDesc(entStatus));
        entProperties.put(MemberConst.ENTNAME, entProperties.remove(MemberConst.NAME));
        entProperties.put(MemberConst.ENT_COUNTRY, entProperties.remove(MemberConst.COUNTRY));
        entProperties.put(MemberConst.ENT_COUNTRY_DESC, entProperties.remove(MemberConst.COUNTRY_DESC));
        entProperties.put(MemberConst.ENT_RISKINFO, entProperties.remove(MemberConst.RISKINFO));

        // 投资人属性处理
        NodeWrapper personDeepCopy = JSON.parseObject(JSON.toJSONString(personNode), NodeWrapper.class);
        Map<String, Object> personProperties = personDeepCopy.getProperties();
        personProperties.remove(MemberConst.INVTYPE);
        personProperties.remove(MemberConst.NODEID);
        AssistantUtils.generateZspId(personProperties, this.parentId);
        personProperties.put(MemberConst.PERSON_RISKINFO, personProperties.remove(MemberConst.RISKINFO));
        personProperties.put(MemberConst.PERSON_COUNTRY, personProperties.remove(MemberConst.COUNTRY));
        personProperties.put(MemberConst.PERSON_COUNTRY_DESC, personProperties.remove(MemberConst.COUNTRY_DESC));

        tempResult.put(MemberConst.RELATION, "直接");
        tempResult.put(MemberConst.RELATION_DENSITY, "半紧密层");
        tempResult.put(MemberConst.PARENT_ID, parentId);
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        tempResult.put(MemberConst._ID, id);
        tempResult.put(MemberConst.INVTYPE, entInvType);

        String conprop = (String) personToParentLink.getProperties().getOrDefault(MemberConst.CON_PROP, "");
        String holderrto = (String) personToParentLink.getProperties().getOrDefault(MemberConst.HOLD_ERRTO, "");
        tempResult.put(MemberConst.CONPROP_PERSON2PARENT, conprop);
        tempResult.put(MemberConst.HOLDERRTO_PERSON2PARENT, holderrto);

        tempResult.putAll(entProperties);
        tempResult.putAll(personProperties);

        return tempResult;
    }

    public List<Map<String, Object>> getMajorControlEnts() {
        return majorControlEnts;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    private enum EntType {
        PARENT,
        MEMBER
    }
}
