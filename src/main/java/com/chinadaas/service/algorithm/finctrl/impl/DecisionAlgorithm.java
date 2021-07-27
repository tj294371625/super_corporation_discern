package com.chinadaas.service.algorithm.finctrl.impl;

import com.chinadaas.entity.DecisionEntity;
import com.chinadaas.model.DecisionModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.finctrl.FinCtrlAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liubc
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.07.27
 */
@Slf4j
@Order(1)
@Component
public class DecisionAlgorithm implements FinCtrlAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public DecisionAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(DecisionModel decisionModel) {
        List<DecisionEntity> decisionEntities = repository.findDecisionNode("");
        decisionModel.screenMaxPropNode(decisionEntities);
        return true;
    }
}
