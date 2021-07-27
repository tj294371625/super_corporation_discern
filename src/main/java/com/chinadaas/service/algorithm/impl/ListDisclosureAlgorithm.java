package com.chinadaas.service.algorithm.impl;

import com.chinadaas.entity.ListDisclosureEntity;
import com.chinadaas.model.ListDisclosureModel;
import com.chinadaas.model.SuperCorporationModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 上市披露算法
 * @createTime 2021.07.13
 */
@Order(4)
@Component
public class ListDisclosureAlgorithm implements SuperCorporationAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public ListDisclosureAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(SuperCorporationModel superCorporationModel) {
        ListDisclosureEntity listDisclosureEntity = repository.findListDisclosureNode(superCorporationModel.getCurrentQueryId());
        ListDisclosureModel listDisclosureModel = new ListDisclosureModel().convertResult(listDisclosureEntity);
        return superCorporationModel.recordListDisclosureResult(listDisclosureModel);
    }
}
