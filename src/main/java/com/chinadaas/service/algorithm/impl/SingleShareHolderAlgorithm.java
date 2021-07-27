package com.chinadaas.service.algorithm.impl;

import com.chinadaas.entity.SingleShareHolderEntity;
import com.chinadaas.model.ChainModel;
import com.chinadaas.model.SingleShareHolderModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 单一大股东算法
 * @createTime 2021.07.13
 */
@Slf4j
@Order(3)
@Component
public class SingleShareHolderAlgorithm implements SuperCorporationAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public SingleShareHolderAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(ChainModel chainModel) {
        SingleShareHolderEntity singleShareHolderEntity = repository.findSingleShareHolderNode(chainModel.getCurrentQueryId());
        SingleShareHolderModel singleShareHolderModel = new SingleShareHolderModel().convertResult(singleShareHolderEntity);
        return chainModel.recordSingleShareHolder(singleShareHolderModel);
    }
}
