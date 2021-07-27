package com.chinadaas.service.algorithm;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.model.SuperCorporationModel;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 母公司识别算法链
 * @createTime 2021.07.01
 */
public interface SuperCorporationAlgorithmChain {

    /**
     * 识别指定类型的点
     *
     * @param entId
     * @param modelType
     * @return
     */
    SuperCorporationModel discernSpecialNode(String entId, ModelType modelType);
}
