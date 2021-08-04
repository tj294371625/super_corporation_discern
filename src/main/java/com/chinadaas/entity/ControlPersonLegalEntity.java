package com.chinadaas.entity;


import com.chinadaas.component.mapper.annotation.CombinationType;
import com.chinadaas.component.wrapper.PathWrapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司最终控股自然人担任法人的企业实体
 * @createTime 2021.08.03
 */
@Setter
@Getter
public class ControlPersonLegalEntity {

    /**
     * 两点间路径
     */
    @CombinationType("path")
    private PathWrapper graph;
}
