package com.chinadaas.entity;

import com.chinadaas.component.mapper.annotation.NodeType;
import com.chinadaas.component.wrapper.NodeWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author liubc
 * @version 1.0.0
 * @description 上市披露实体
 * @createTime 2021.07.13
 */
@Getter
@Setter
@ToString
public class ListDisclosureEntity {

    @NodeType("result")
    private NodeWrapper listDisclosureNode;
}
