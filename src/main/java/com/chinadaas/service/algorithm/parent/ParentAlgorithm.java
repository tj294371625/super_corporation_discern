package com.chinadaas.service.algorithm.parent;

import com.chinadaas.model.ParentModel;

/**
 * @author liubc
 * @version 1.0.0
 * @description 母公司算法
 * @createTime 2021.07.13
 */
public interface ParentAlgorithm {

    /**
     * 计算获得母公司模型
     *
     * @param parentModel
     * @return
     */
    boolean calculation(ParentModel parentModel);
}
