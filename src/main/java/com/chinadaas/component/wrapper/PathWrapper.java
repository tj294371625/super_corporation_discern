package com.chinadaas.component.wrapper;

import com.alibaba.fastjson.annotation.JSONField;
import com.chinadaas.component.mapper.annotation.LinksType;
import com.chinadaas.component.mapper.annotation.NodesType;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field("nodes")
    @NodesType("nodes")
    @JSONField(name = "nodes")
    Set<NodeWrapper> nodeWrappers = Sets.newHashSet();

    @Field("links")
    @LinksType("links")
    @JSONField(name = "links")
    Set<LinkWrapper> linkWrappers = Sets.newHashSet();

}
