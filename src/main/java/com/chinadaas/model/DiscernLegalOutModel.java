package com.chinadaas.model;

import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.DiscernLegalOutEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司法人对外直接控制的公司
 * @createTime 2021.08.03
 */
public class DiscernLegalOutModel {

    /**
     * 查询输入的母公司标识
     */
    private final String parentId;

    /**
     * 法人节点信息
     */
    private NodeWrapper legalNode;

    /**
     * 法人对外直接控股企业
     */
    private List<Map<String, Object>> legalControlEnts;

    private ModelStatus status;

    public DiscernLegalOutModel(String parentId) {
        this.parentId = parentId;
        this.legalControlEnts = Lists.newArrayList();
        this.status = ModelStatus.NO_RESULT;
    }

    public DiscernLegalOutModel convertEntity2Model(DiscernLegalOutEntity entity) {
        if (Objects.isNull(entity)) {
            return this;
        }

        PathWrapper graph = entity.getGraph();
        this.legalNode = graph.getNodeWrappers().stream()
                .filter(node -> node.getType() == NodeType.PERSON)
                .findFirst()
                .orElse(null);

        return this;
    }

    public DiscernLegalOutModel queryLegalControlEnts(BiFunction<String, String, List<Map>> commonPersonControlEnt) {
        String zsId = legalNode.obtainZsId();
        List<Map> tempResultList = commonPersonControlEnt.apply(zsId, parentId);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return this;
        }

        // zs: 此时标记为结果存在状态
        this.status = ModelStatus.HAVE_RESULT;

        // 获取法人控制的母公司
        List<Map> controlParents = legalControlEnts.stream()
                .filter(ent -> Objects.nonNull(ent.get(MemberConst.PARENT_ID)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(controlParents)) {
            for (Map controlParent : controlParents) {
                legalControlEnts.add(processEntInfo(controlParent, EntType.PARENT));
            }
        }

        // 获取法人控制的成员公司
        List<Map> controlMembers = legalControlEnts.stream()
                .filter(ent -> Objects.isNull(ent.get(MemberConst.PARENT_ID)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(controlMembers)) {
            for (Map controlMember : controlMembers) {
                legalControlEnts.add(processEntInfo(controlMember, EntType.MEMBER));
            }
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> processEntInfo(Map entInfo, EntType type) {
        Map<String, Object> tempResult = Maps.newHashMap();

        Map property;
        if (EntType.PARENT.equals(type)) {
            property = (Map) entInfo.get(MemberConst.SOURCE_PROPERTY);
            tempResult.put(MemberConst.PATH, entInfo.get(MemberConst.CTRL2PARENT_PATH));
            tempResult.put(MemberConst.FINAL_CGZB, MemberConst.CTRL2PARENT_CGZB);
        } else {
            property = (Map) entInfo.get(MemberConst.PARENT_PROPERTY);
            tempResult.put(MemberConst.PATH, entInfo.get(MemberConst.CTRL2SOURCE_PATH));
            tempResult.put(MemberConst.FINAL_CGZB, MemberConst.CTRL2SOURCE_CGZB);
        }

        tempResult.putAll(property);
        tempResult.put(MemberConst.ENTNAME, tempResult.remove(MemberConst.NAME));
        String entStatus = (String) tempResult.get(MemberConst.ENTSTATUS);
        tempResult.put(MemberConst.ENTSTATUS_DESC, AssistantUtils.getEntStatusDesc(entStatus));

        tempResult.putAll(legalNode.getProperties());

        tempResult.put(MemberConst.PARENT_ID, parentId);

        return tempResult;
    }

    private enum EntType {
        PARENT,
        MEMBER
    }
}
