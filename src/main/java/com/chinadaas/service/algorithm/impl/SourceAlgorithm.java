package com.chinadaas.service.algorithm.impl;

import com.chinadaas.entity.SourceEntity;
import com.chinadaas.model.SuperCorporationModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 识别输入企业
 * @createTime 2021.07.13
 */
@Order(1)
@Slf4j
@Component
public class SourceAlgorithm implements SuperCorporationAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public SourceAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(SuperCorporationModel superCorporationModel) {
        SourceEntity sourceEntity = repository.findSourceNode(superCorporationModel.getCurrentQueryId());
        return superCorporationModel.recordSourceResult(sourceEntity);
    }

}
