package com.chinadaas.entity;

import com.chinadaas.component.mapper.annotation.CombinationType;
import com.chinadaas.component.wrapper.PathWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author lawliet
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.08.05
 */
@Setter
@Getter
@ToString
public class StaffEntity {

    /**
     * 两点间路径
     */
    @CombinationType("path")
    private PathWrapper graph;
}
