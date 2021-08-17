package com.chinadaas.model;

import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.Neo4jResultParseUtils;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.ControlPersonLegalEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司最终控股自然人担任法人的企业
 * @createTime 2021.08.03
 */
public class ControlPersonLegalModel {

    private String parentId;

    private List<NodeWrapper> legalMemberNodes;

    private List<Map<String, Object>> resultList;

    private Map finalControlPerson;

    private ModelStatus resultStatus;

    public ControlPersonLegalModel(String parentId) {
        this.parentId = parentId;
        this.resultStatus = ModelStatus.NO_RESULT;
        this.legalMemberNodes = Lists.newArrayList();
        this.resultList = Lists.newArrayList();
    }

    public ControlPersonLegalModel converEntity2Model(ControlPersonLegalEntity entity, Map finalControlPerson) {
        if (Objects.isNull(entity) || CollectionUtils.isEmpty(finalControlPerson)) {
            return this;
        }

        if (entity.getGraph().emptyPath()) {
            return this;
        }

        this.resultStatus = ModelStatus.HAVE_RESULT;

        PathWrapper graph = entity.getGraph();
        Set<NodeWrapper> nodes = graph.getNodeWrappers();
        Neo4jResultParseUtils.setFirstNode(nodes, this.parentId);

        this.legalMemberNodes = nodes.stream()
                .filter(node -> node.getType() == NodeType.ENT)
                .filter(node -> !node.isInnode())
                .collect(Collectors.toList());

        this.finalControlPerson = finalControlPerson;

        return this;
    }


    public ControlPersonLegalModel processProperties() {

        for (NodeWrapper legalMemberNode : legalMemberNodes) {
            Map<String, Object> tempResult = Maps.newHashMap();

            // 记录企业的invType
            Object entInvType;

            // 企业属性处理
            Map<String, Object> memberProperties = legalMemberNode.getProperties();
            entInvType = memberProperties.remove(MemberConst.INVTYPE);
            memberProperties.remove(MemberConst.ZSID);
            String entStatus = (String) memberProperties.get(MemberConst.ENTSTATUS);
            memberProperties.put(MemberConst.ENTSTATUS_DESC, AssistantUtils.getEntStatusDesc(entStatus));
            memberProperties.put(MemberConst.ENTNAME, memberProperties.remove(MemberConst.NAME));
            memberProperties.put(MemberConst.ENTID, memberProperties.remove(MemberConst.NODEID));
            memberProperties.put(MemberConst.ENT_COUNTRY, memberProperties.remove(MemberConst.COUNTRY));
            memberProperties.put(MemberConst.ENT_COUNTRY_DESC, memberProperties.remove(MemberConst.COUNTRY_DESC));
            memberProperties.put(MemberConst.ENT_RISKINFO, memberProperties.remove(MemberConst.RISKINFO));

            // 自然人属性处理
            Map personProperties = (Map) finalControlPerson.get(MemberConst.FIN_CTRL_PROPERTY);
            personProperties.remove(MemberConst.INVTYPE);
            personProperties.remove(MemberConst.NODEID);
            AssistantUtils.generateZspId(personProperties, this.parentId);
            personProperties.put(MemberConst.PERSON_RISKINFO, personProperties.remove(MemberConst.RISKINFO));
            personProperties.put(MemberConst.PERSON_COUNTRY, personProperties.remove(MemberConst.COUNTRY));
            personProperties.put(MemberConst.PERSON_COUNTRY_DESC, personProperties.remove(MemberConst.COUNTRY_DESC));

            tempResult.put(MemberConst.PARENT_ID, parentId);
            tempResult.put(MemberConst.RELATION, "直接");
            tempResult.put(MemberConst.RELATION_DENSITY, "半紧密层");
            String id = UUID.randomUUID().toString().replaceAll("-", "");
            tempResult.put(MemberConst._ID, id);
            Map ctrl2ParentPath = (Map) finalControlPerson.get(MemberConst.CTRL2PARENT_PATH);
            String ctrl2ParentCgzb = (String) finalControlPerson.get(MemberConst.CTRL2PARENT_CGZB);
            tempResult.put(MemberConst.PATH, ctrl2ParentPath);
            tempResult.put(MemberConst.FINAL_CGZB, ctrl2ParentCgzb);
            tempResult.put(MemberConst.INVTYPE, entInvType);
            tempResult.putAll(memberProperties);
            tempResult.putAll(personProperties);

            resultList.add(tempResult);
        }

        return this;
    }

    public List<Map<String, Object>> getResultList() {
        return resultList;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }
}
