package com.chinadaas.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author lawliet
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
     * 临时目标（临时母公司 or 临时最终控股股东）
     */
    @Field("temp_entid")
    private String tempEntId;
    @Field("temp_name")
    private String tempName;

    /**
     * 目标（母公司 or 最终控股股东）
     */
    @Field("target_entid")
    private String targetEntId;
    @Field("target_name")
    private String targetName;

    @Field("source_to_temp_layer")
    private long source2TempLayer;

    @Field("source_to_target_layer")
    private long source2TargetLayer;

    @Field("target_type")
    private String targetType;
}
