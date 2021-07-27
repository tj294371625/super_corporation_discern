package com.chinadaas.service.algorithm.parent;

import com.chinadaas.model.ParentModel;

/**
 * @author liubc
 * @version 1.0.0
 * @description 母公司识别算法链
 * @createTime 2021.07.01
 */
public interface ParentAlgorithmChain {

    /**
     * 识别母公司
     *
     * @param entId
     * @return
     */
    ParentModel discernParentNode(String entId);
}
