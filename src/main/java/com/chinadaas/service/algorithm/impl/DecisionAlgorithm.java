package com.chinadaas.service.algorithm.impl;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.entity.DecisionEntity;
import com.chinadaas.model.DecisionModel;
import com.chinadaas.model.SuperCorporationModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 决策权算法
 * @createTime 2021.07.13
 */
@Order(2)
@Slf4j
@Component
public class DecisionAlgorithm implements SuperCorporationAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public DecisionAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(SuperCorporationModel superCorporationModel) {
        ModelType businessType = superCorporationModel.getBusinessType();
        List<DecisionEntity> decisionEntities = repository.findDecisionNode(superCorporationModel.getCurrentQueryId(), businessType);
        DecisionModel decisionModel = new DecisionModel().screenMaxPropNode(decisionEntities);
        return superCorporationModel.recordDecisionResult(decisionModel, businessType);
    }

}
