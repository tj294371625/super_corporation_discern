package com.chinadaas.model;

import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.entity.PersonOutControlEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司最终控股自然人对外控制的企业
 * @createTime 2021.08.03
 */
public class PersonOutControlModel {

    private String parentId;

    private List<Map<String, Object>> resultList;

    private Map finalControlPerson;

    private List<Map> tempResultList;

    private ModelStatus status;

    public PersonOutControlModel(String parentId, Map finalControlPerson) {
        this.status = ModelStatus.NO_RESULT;
        this.tempResultList = Lists.newArrayList();
        this.parentId = parentId;
        this.resultList = Lists.newArrayList();
        this.finalControlPerson = finalControlPerson;
    }

    public PersonOutControlModel convertEntity2Model(PersonOutControlEntity entity) {
        if (Objects.isNull(entity)) {
            return this;
        }

        this.tempResultList = entity.getTempResultList();
        if (CollectionUtils.isEmpty(this.tempResultList)) {
            return this;
        }

        this.status = ModelStatus.HAVE_RESULT;

        return this;
    }

    @SuppressWarnings("unchecked")
    public PersonOutControlModel processProperties() {

        for (Map tempResult : tempResultList) {
            Map<String, Object> finalMember = Maps.newHashMap();

            // 记录企业的invType
            Object entInvType;

            // 企业属性处理
            Map<String, Object> memberProperties = (Map<String, Object>) tempResult.get(MemberConst.SOURCE_PROPERTY);
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

            finalMember.put(MemberConst.PARENT_ID, parentId);
            finalMember.put(MemberConst.RELATION, "直接");
            finalMember.put(MemberConst.RELATION_DENSITY, "半紧密层");
            String id = UUID.randomUUID().toString().replaceAll("-", "");
            finalMember.put(MemberConst._ID, id);

            Map ctrl2ParentPath = (Map) finalControlPerson.get(MemberConst.CTRL2PARENT_PATH);
            String ctrl2ParentCgzb = (String) finalControlPerson.get(MemberConst.CTRL2PARENT_CGZB);
            finalMember.put(MemberConst.CTRL2PARENT_PATH, ctrl2ParentPath);
            finalMember.put(MemberConst.CTRL2PARENT_CGZB, ctrl2ParentCgzb);

            Map ctrl2sourcePath = (Map) tempResult.get(MemberConst.CTRL2SOURCE_PATH);
            String ctrl2SourceCgzb = (String) tempResult.get(MemberConst.CTRL2SOURCE_CGZB);
            finalMember.put(MemberConst.CTRL2SOURCE_PATH, ctrl2sourcePath);
            finalMember.put(MemberConst.CTRL2SOURCE_CGZB, ctrl2SourceCgzb);

            String parent2SourceRelation = (String) tempResult.get(MemberConst.PARENT2SOURCE_RELATION);
            finalMember.put(MemberConst.PARENT2SOURCE_RELATION, parent2SourceRelation);

            finalMember.put(MemberConst.INVTYPE, entInvType);
            finalMember.putAll(memberProperties);
            finalMember.putAll(personProperties);

            resultList.add(finalMember);
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
