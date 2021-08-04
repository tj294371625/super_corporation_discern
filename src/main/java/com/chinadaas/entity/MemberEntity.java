package com.chinadaas.entity;

import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.component.mapper.annotation.FieldType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 子公司实体
 * @createTime 2021.08.03
 */
@ToString
@Setter
@Getter
public class MemberEntity {

    @FieldType(MemberConst.PARENT2SOURCE_PATH)
    private Map parent2SourcePath;

    @FieldType(MemberConst.PARENT2SOURCE_CGZB)
    private String finalCgzb;

    @FieldType(MemberConst.PARENT_ID)
    private String parentId;

    @FieldType(MemberConst.SOURCE_PROPERTY)
    private Map member;

    @FieldType(MemberConst.PARENT2SOURCE_RELATION)
    private String parent2SourceRelation;
}
