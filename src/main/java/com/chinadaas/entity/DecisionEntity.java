package com.chinadaas.entity;

import com.chinadaas.component.mapper.annotation.FieldType;
import com.chinadaas.component.mapper.annotation.NodeType;
import com.chinadaas.component.wrapper.NodeWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author liubc
 * @version 1.0.0
 * @description 决策权实体
 * @createTime 2021.07.01
 */
@Getter
@Setter
@ToString
public class DecisionEntity {

    /**
     * 决策点到输入点的最短路径长度
     */
    @FieldType("length")
    private Long length;

    /**
     * 决策点对输入点的最终持股占比
     */
    @FieldType("cgzb")
    private Double finalCgzb;

    /**
     * 决策权点
     */
    @NodeType("result")
    private NodeWrapper decisionNode;

}
