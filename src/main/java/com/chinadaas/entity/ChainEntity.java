package com.chinadaas.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author liubc
 * @version 1.0.0
 * @description 链路实体
 * @createTime 2021.07.01
 */
@Setter
@Getter
@ToString
public class ChainEntity {

    /**
     * 源（输入企业）
     */
    @Field("source_entid")
    private String sourceEntId;
    @Field("source_name")
    private String sourceName;

    /**
     * 目标（临时母公司）
     */
    @Field("target_entid")
    private String targetEntId;
    @Field("target_name")
    private String targetName;

    /**
     * 组（母公司）
     */
    @Field("group_entid")
    private String groupEntId;
    @Field("group_name")
    private String groupName;
}
