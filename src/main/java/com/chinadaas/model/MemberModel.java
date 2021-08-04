package com.chinadaas.model;

import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.entity.MemberEntity;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 子公司模型
 * @createTime 2021.08.03
 */
@Slf4j
public class MemberModel {

    private Map parent2SourcePath;

    private String parent2SourceRelation;

    private String finalCgzb;

    private String parentId;

    private Map member;

    private Map<String, Object> mergeResults;

    private ModelStatus resultStatus;

    public MemberModel() {
        this.resultStatus = ModelStatus.NO_RESULT;
        this.mergeResults = Maps.newHashMap();
    }

    public MemberModel convertEntity2Model(MemberEntity entity) {
        if (Objects.isNull(entity)) {
            return this;
        }

        this.parent2SourcePath = entity.getParent2SourcePath();
        this.parent2SourceRelation = entity.getParent2SourceRelation();
        this.finalCgzb = entity.getFinalCgzb();
        this.parentId = entity.getParentId();
        this.member = entity.getMember();
        this.resultStatus = ModelStatus.HAVE_RESULT;

        return this;
    }

    public MemberModel calMemberResult() {
        mergeResults.putAll(this.member);

        mergeResults.put(MemberConst.ENTNAME, mergeResults.remove(MemberConst.NAME));
        mergeResults.put("relation_density", "紧密层");
        mergeResults.put(MemberConst.PARENT2SOURCE_RELATION, this.parent2SourceRelation);
        mergeResults.put(MemberConst.PARENT_ID, this.parentId);
        mergeResults.put(MemberConst.PATH, this.parent2SourcePath);
        mergeResults.put(MemberConst.FINAL_CGZB, this.finalCgzb);
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        mergeResults.put(MemberConst._ID, id);

        return this;
    }

    public Map<String, Object> getMergeResults() {
        return mergeResults;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }
}
