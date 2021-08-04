package com.chinadaas.model;

import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.ControlPersonLegalEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司最终控股自然人担任法人的企业
 * @createTime 2021.08.03
 */
public class ControlPersonLegalModel {

    private static final int ENT_TYPE = 1;
    private static final int PERSON_TYPE = 2;

    private String parentId;

    private List<NodeWrapper> legalMemberNodes;

    private List<Map<String, Object>> resultList;

    private Map finalControlPerson;

    private ModelStatus status;

    public ControlPersonLegalModel(String parentId) {
        this.parentId = parentId;
        this.status = ModelStatus.NO_RESULT;
        this.legalMemberNodes = Lists.newArrayList();
        this.resultList = Lists.newArrayList();
    }

    public ControlPersonLegalModel converEntity2Model(ControlPersonLegalEntity entity, Map finalControlPerson) {
        if (Objects.isNull(entity)) {
            return this;
        }

        this.status = ModelStatus.HAVE_RESULT;

        PathWrapper graph = entity.getGraph();
        Set<NodeWrapper> nodes = graph.getNodeWrappers();

        this.legalMemberNodes = nodes.stream()
                .filter(node -> node.getType() == ENT_TYPE)
                .collect(Collectors.toList());

        this.finalControlPerson = finalControlPerson;

        return this;
    }


    public ControlPersonLegalModel processProperties() {

        for (NodeWrapper legalMemberNode : legalMemberNodes) {
            Map<String, Object> legalMember = Maps.newHashMap();

            Map<String, Object> properties = legalMemberNode.getProperties();
            legalMember.putAll(properties);
            String entStatusDesc = AssistantUtils.getEntStatusDesc((String) legalMember.get(MemberConst.ENTSTATUS));
            legalMember.put(MemberConst.ENTSTATUS_DESC, entStatusDesc);
            legalMember.put(MemberConst.ENTNAME, properties.remove(MemberConst.NAME));

            Map finalControlProperty = (Map) finalControlPerson.get(MemberConst.FIN_CTRL_PROPERTY);
            legalMember.putAll(finalControlProperty);

            Map ctrl2ParentPath = (Map) finalControlPerson.get(MemberConst.CTRL2PARENT_PATH);
            String ctrl2ParentCgzb = (String) finalControlPerson.get(MemberConst.CTRL2PARENT_CGZB);
            legalMember.put(MemberConst.PARENT_ID, parentId);
            legalMember.put(MemberConst.PATH, ctrl2ParentPath);
            legalMember.put(MemberConst.FINAL_CGZB, ctrl2ParentCgzb);

            resultList.add(legalMember);
        }

        return this;
    }

    public List<Map<String, Object>> getResultList() {
        return resultList;
    }

    public ModelStatus getStatus() {
        return status;
    }
}
