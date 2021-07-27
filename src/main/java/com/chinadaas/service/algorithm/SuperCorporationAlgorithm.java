package com.chinadaas.service.algorithm;

import com.chinadaas.model.SuperCorporationModel;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团算法
 * @createTime 2021.07.13
 */
public interface SuperCorporationAlgorithm {

    /**
     * 计算获得指定模型
     *
     * @param superCorporationModel
     * @return
     */
    boolean calculation(SuperCorporationModel superCorporationModel);
}
