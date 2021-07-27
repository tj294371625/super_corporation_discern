package com.chinadaas.entity;

import com.chinadaas.component.mapper.annotation.NodeType;
import com.chinadaas.component.wrapper.NodeWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author liubc
 * @version 1.0.0
 * @description 输入企业实体
 * @createTime 2021.07.05
 */
@Setter
@Getter
@ToString
public class SourceEntity {

    @NodeType("source")
    private NodeWrapper sourceNode;
}
