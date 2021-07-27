package com.chinadaas.service.algorithm.finctrl.impl;

import com.chinadaas.model.DecisionModel;
import com.chinadaas.service.algorithm.finctrl.FinCtrlAlgorithm;
import com.chinadaas.service.algorithm.finctrl.FinCtrlAlgorithmChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubc
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.07.27
 */
@Slf4j
@Service
public class FinCtrlAlgorithmChainImpl implements FinCtrlAlgorithmChain {

    private final List<FinCtrlAlgorithm> finCtrlAlgorithms;

    @Autowired
    public FinCtrlAlgorithmChainImpl(List<FinCtrlAlgorithm> finCtrlAlgorithms) {
        this.finCtrlAlgorithms = finCtrlAlgorithms;
    }


    @Override
    public DecisionModel discernFinCtrlNode(String entId) {
        DecisionModel decisionModel = new DecisionModel();

        // 职责链
        for (FinCtrlAlgorithm finCtrlAlgorithm : finCtrlAlgorithms) {
            if (BooleanUtils.isFalse(finCtrlAlgorithm.calculation(decisionModel))) {
                break;
            }
        }

        return decisionModel;
    }
}
