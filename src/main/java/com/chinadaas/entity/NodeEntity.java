package com.chinadaas.entity;

import com.chinadaas.component.mapper.annotation.NodeType;
import com.chinadaas.component.wrapper.NodeWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author liubc
 * @version 1.0.0
 * @description 节点实体
 * @createTime 2021.07.23
 */
@Setter
@Getter
@ToString
public class NodeEntity {

    @NodeType("node")
    private NodeWrapper node;
}
