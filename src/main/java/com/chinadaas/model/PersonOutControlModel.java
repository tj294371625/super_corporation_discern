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

    public PersonOutControlModel converEntity2Model(PersonOutControlEntity entity) {
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

    public PersonOutControlModel processProperties() {

        for (Map tempResult : tempResultList) {
            Map<String, Object> finalMember = Maps.newHashMap();
            Map sourceProperty = (Map) tempResult.get(MemberConst.SOURCE_PROPERTY);
            Object ctrl2SourcePath = tempResult.get(MemberConst.CTRL2SOURCE_PATH);
            String ctrl2SourceCgzb = (String) tempResult.get(MemberConst.CTRL2PARENT_CGZB);

            finalMember.putAll(sourceProperty);
            finalMember.put(MemberConst.ENTNAME, finalMember.remove(MemberConst.NAME));
            String entStatusDesc = AssistantUtils.getEntStatusDesc((String) sourceProperty.get(MemberConst.ENTSTATUS));
            finalMember.put(MemberConst.ENTSTATUS_DESC, entStatusDesc);
            finalMember.put(MemberConst.CTRL2SOURCE_PATH, ctrl2SourcePath);
            finalMember.put(MemberConst.CTRL2PARENT_CGZB, ctrl2SourceCgzb);

            Map finalControlProperty = (Map) finalControlPerson.get(MemberConst.FIN_CTRL_PROPERTY);
            finalMember.putAll(finalControlProperty);

            Map ctrl2ParentPath = (Map) finalControlPerson.get(MemberConst.CTRL2PARENT_PATH);
            String ctrl2ParentCgzb = (String) finalControlPerson.get(MemberConst.CTRL2PARENT_CGZB);
            finalMember.put(MemberConst.PARENT_ID, parentId);
            finalMember.put(MemberConst.CTRL2PARENT_PATH, ctrl2ParentPath);
            finalMember.put(MemberConst.CTRL2PARENT_CGZB, ctrl2ParentCgzb);
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
