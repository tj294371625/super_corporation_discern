package com.chinadaas.service.algorithm.finctrl;

import com.chinadaas.model.DecisionModel;

/**
 * @author liubc
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.07.27
 */
public interface FinCtrlAlgorithmChain {

    DecisionModel discernFinCtrlNode(String entId);
}
