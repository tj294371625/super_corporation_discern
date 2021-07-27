package com.chinadaas.service.algorithm.parent.impl;

import com.chinadaas.model.ParentModel;
import com.chinadaas.service.algorithm.parent.ParentAlgorithmChain;
import com.chinadaas.service.algorithm.parent.ParentAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubc
 * @version 1.0.0
 * @description
 * @createTime 2021.07.01
 */
@Slf4j
@Service
public class ParentAlgorithmChainImpl implements ParentAlgorithmChain {

    private final List<ParentAlgorithm> parentAlgorithms;

    @Autowired
    public ParentAlgorithmChainImpl(List<ParentAlgorithm> parentAlgorithms) {
        this.parentAlgorithms = parentAlgorithms;
    }

    @Override
    public ParentModel discernParentNode(String entId) {

        ParentModel parentModel = new ParentModel(entId);

        // 职责链
        for (ParentAlgorithm parentAlgorithm : parentAlgorithms) {
            if (BooleanUtils.isFalse(parentAlgorithm.calculation(parentModel))) {
                break;
            }
        }

        return parentModel;
    }
}
