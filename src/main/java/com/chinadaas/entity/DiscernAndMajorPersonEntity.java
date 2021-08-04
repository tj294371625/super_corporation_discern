package com.chinadaas.entity;


import com.chinadaas.component.mapper.annotation.CombinationType;
import com.chinadaas.component.wrapper.PathWrapper;
import lombok.Getter;
import lombok.Setter;


/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司及主要投资者个人共同控制企业实体
 * @createTime 2021.08.03
 */
@Setter
@Getter
public class DiscernAndMajorPersonEntity {

    /**
     * 两点间路径
     */
    @CombinationType("path")
    private PathWrapper graph;
}
