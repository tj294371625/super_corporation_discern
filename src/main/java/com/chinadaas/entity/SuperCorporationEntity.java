package com.chinadaas.entity;

import com.chinadaas.component.wrapper.PathWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团实体
 * @createTime 2021.07.29
 */
@Setter
@Getter
@ToString
public class SuperCorporationEntity {

    /**
     * 输入企业标识
     */
    @Field("entid")
    private String entId;

    /**
     * 输入企业名称
     */
    @Field("entname")
    private String entName;

    /**
     * 输入企业属性
     */
    @Field("source_property")
    private Map<String, Object> sourceProperty;

    /**
     * 最终控股股东标识
     */
    @Field("fin_ctrl_id")
    private String finCtrlId;

    /**
     * 最终控股股东名称
     */
    @Field("fin_ctrl_name")
    private String finCtrlName;

    /**
     * 最终控股股东属性
     */
    @Field("fin_ctrl_property")
    private Map<String, Object> finCtrlProperty;

    /**
     * 母公司和输入企业的关系：直接 or 间接
     */
    @Field("parent2source_relation")
    private String parent2SourceRelation;

    /**
     * 母公司标识
     */
    @Field("parent_id")
    private String parentId;

    /**
     * 母公司名称
     */
    @Field("parent_name")
    private String parentName;

    /**
     * 母公司注册号
     */
    @Field("parent_regno")
    private String parentRegno;

    /**
     * 母公司统一信用代码
     */
    @Field("parent_creditcode")
    private String parentCreditcode;

    /**
     * 最终控股股东到母公司的路径
     */
    @Field("ctrl2parent_path")
    private PathWrapper ctrl2ParentPath;

    /**
     * 母公司到输入企业的路径
     */
    @Field("parent2source_path")
    private PathWrapper parent2SourcePath;

    /**
     * 最终控股股东到输入企业的路径
     */
    @Field("ctrl2source_path")
    private PathWrapper ctrl2SourcePath;

    /**
     * 母公司属性
     */
    @Field("parent_property")
    private Map<String, Object> parentProperty;

    /**
     * 母公司到输入企业的持股占比
     */
    @Field("parent2source_cgzb")
    private String parent2SourceCgzb;

    /**
     * 最终控股股东到输入企业的持股占比
     */
    @Field("ctrl2source_cgzb")
    private String ctrl2SourceCgzb;

    /**
     * 最终控股股东到母公司的持股占比
     */
    @Field("ctrl2parent_cgzb")
    private String ctrl2ParentCgzb;

    /**
     * 高管关联码
     */
    @Field("emid")
    private String emId;

}
