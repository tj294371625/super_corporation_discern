package com.chinadaas.service.algorithm.impl;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.model.ChainModel;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithmChain;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.01
 */
@Slf4j
@Service
public class SuperCorporationAlgorithmChainImpl implements SuperCorporationAlgorithmChain {

    private final List<SuperCorporationAlgorithm> superCorporationAlgorithms;

    @Autowired
    public SuperCorporationAlgorithmChainImpl(List<SuperCorporationAlgorithm> superCorporationAlgorithms) {
        this.superCorporationAlgorithms = superCorporationAlgorithms;
    }

    @Override
    public ChainModel discernSpecialTypeChain(String entId, ModelType modelType) {
        ChainModel chainModel = new ChainModel(entId, modelType);

        // 职责链
        for (SuperCorporationAlgorithm superCorporationAlgorithm : superCorporationAlgorithms) {
            if (BooleanUtils.isFalse(superCorporationAlgorithm.calculation(chainModel))) {
                break;
            }
        }

        return chainModel;
    }
}
