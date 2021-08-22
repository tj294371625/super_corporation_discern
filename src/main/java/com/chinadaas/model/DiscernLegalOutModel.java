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
import java.util.UUID;
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

    private ModelStatus resultStatus;

    public DiscernLegalOutModel(String parentId) {
        this.parentId = parentId;
        this.legalControlEnts = Lists.newArrayList();
        this.resultStatus = ModelStatus.NO_RESULT;
    }

    public DiscernLegalOutModel convertEntity2Model(DiscernLegalOutEntity entity) {
        if (Objects.isNull(entity)) {
            return this;
        }

        if (entity.getGraph().emptyPath()) {
            return this;
        }

        this.resultStatus = ModelStatus.HAVE_RESULT;

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
        this.resultStatus = ModelStatus.HAVE_RESULT;

        // 获取法人控制的母公司
        List<Map> controlParents = tempResultList.stream()
                .filter(ent -> Objects.nonNull(ent.get(MemberConst.PARENT_ID)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(controlParents)) {
            for (Map controlParent : controlParents) {
                legalControlEnts.add(processEntInfo(controlParent, EntType.PARENT));
            }
        }

        // 获取法人控制的成员公司
        List<Map> controlMembers = tempResultList.stream()
                .filter(ent -> Objects.isNull(ent.get(MemberConst.PARENT_ID)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(controlMembers)) {
            for (Map controlMember : controlMembers) {
                legalControlEnts.add(processEntInfo(controlMember, EntType.MEMBER));
            }
        }

        return this;
    }

    public List<Map<String, Object>> getLegalControlEnts() {
        return legalControlEnts;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> processEntInfo(Map entInfo, EntType type) {
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

        // 法人属性处理
        Map<String, Object> personProperties = legalNode.getProperties();
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
        tempResult.putAll(entProperties);
        tempResult.putAll(personProperties);

        return tempResult;
    }

    private enum EntType {
        PARENT,
        MEMBER
    }
}
