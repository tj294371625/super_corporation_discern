package com.chinadaas.service.algorithm.parent.impl;

import com.chinadaas.entity.SourceEntity;
import com.chinadaas.model.ParentModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.parent.ParentAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author liubc
 * @version 1.0.0
 * @description 识别输入企业
 * @createTime 2021.07.13
 */
@Order(1)
@Slf4j
@Component
public class SourceAlgorithm implements ParentAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public SourceAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(ParentModel parentModel) {
        SourceEntity sourceEntity = repository.findSourceNode(parentModel.getCurrentQueryId());
        return parentModel.recordSourceResult(sourceEntity);
    }
}
