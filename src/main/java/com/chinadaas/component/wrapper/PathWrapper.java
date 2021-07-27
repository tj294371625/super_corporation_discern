package com.chinadaas.component.wrapper;

import com.chinadaas.component.mapper.annotation.LinksType;
import com.chinadaas.component.mapper.annotation.NodesType;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 路径
 * @createTime 2021.07.01
 */
@Getter
@Setter
@ToString
public class PathWrapper {

    @NodesType("nodes")
    Set<NodeWrapper> nodeWrappers = Sets.newHashSet();

    @LinksType("links")
    Set<LinkWrapper> linkWrappers = Sets.newHashSet();

}
