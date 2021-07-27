package com.chinadaas.service.algorithm.impl;

import com.chinadaas.entity.SingleShareHolderEntity;
import com.chinadaas.model.SuperCorporationModel;
import com.chinadaas.model.SingleShareHolderModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 单一大股东算法
 * @createTime 2021.07.13
 */
@Order(3)
@Component
public class SingleShareHolderAlgorithm implements SuperCorporationAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public SingleShareHolderAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(SuperCorporationModel superCorporationModel) {
        SingleShareHolderEntity singleShareHolderEntity = repository.findSingleShareHolderNode(superCorporationModel.getCurrentQueryId());
        SingleShareHolderModel singleShareHolderModel = new SingleShareHolderModel().convertResult(singleShareHolderEntity);
        return superCorporationModel.recordSingleShareHolder(singleShareHolderModel);
    }
}
