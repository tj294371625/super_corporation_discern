package com.chinadaas.service.algorithm;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.model.ChainModel;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团识别算法链
 * @createTime 2021.07.01
 */
public interface SuperCorporationAlgorithmChain {

    /**
     * 指定类型链路识别
     *
     * @param entId
     * @param modelType
     * @return
     */
    ChainModel discernSpecifyTypeChain(String entId, ModelType modelType);
}
