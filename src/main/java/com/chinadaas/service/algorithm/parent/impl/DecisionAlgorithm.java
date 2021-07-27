package com.chinadaas.service.algorithm.parent.impl;

import com.chinadaas.entity.DecisionEntity;
import com.chinadaas.model.DecisionModel;
import com.chinadaas.model.ParentModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.parent.ParentAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liubc
 * @version 1.0.0
 * @description 决策权算法
 * @createTime 2021.07.13
 */
@Order(2)
@Slf4j
@Component
public class DecisionAlgorithm implements ParentAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public DecisionAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(ParentModel parentModel) {
        List<DecisionEntity> decisionEntities = repository.findDecisionNode(parentModel.getCurrentQueryId());
        DecisionModel decisionModel = new DecisionModel().screenMaxPropNode(decisionEntities);
        return parentModel.recordDecisionResult(decisionModel);
    }
}
