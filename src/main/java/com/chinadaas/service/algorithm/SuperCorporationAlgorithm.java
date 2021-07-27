package com.chinadaas.service.algorithm;

import com.chinadaas.model.ChainModel;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团算法
 * @createTime 2021.07.13
 */
public interface SuperCorporationAlgorithm {

    /**
     * 计算获得链路模型
     *
     * @param chainModel
     * @return
     */
    boolean calculation(ChainModel chainModel);
}
