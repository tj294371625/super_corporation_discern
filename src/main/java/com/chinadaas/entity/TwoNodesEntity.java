package com.chinadaas.entity;


import com.chinadaas.component.mapper.annotation.CombinationType;
import com.chinadaas.component.mapper.annotation.FieldType;
import com.chinadaas.component.wrapper.PathWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.28
 */
@Getter
@Setter
@ToString
public class TwoNodesEntity {

    /**
     * 两点间路径
     */
    @CombinationType("path")
    private PathWrapper twoNodesPath;

    /**
     * 两点间持股比例
     */
    @FieldType("cgzb")
    private Double twoNodesCgzb;

}