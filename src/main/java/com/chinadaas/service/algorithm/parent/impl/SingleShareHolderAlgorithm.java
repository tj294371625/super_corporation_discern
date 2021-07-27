package com.chinadaas.service.algorithm.parent.impl;

import com.chinadaas.entity.SingleShareHolderEntity;
import com.chinadaas.model.ParentModel;
import com.chinadaas.model.SingleShareHolderModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.parent.ParentAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author liubc
 * @version 1.0.0
 * @description 单一大股东算法
 * @createTime 2021.07.13
 */
@Order(3)
@Component
public class SingleShareHolderAlgorithm implements ParentAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public SingleShareHolderAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(ParentModel parentModel) {
        SingleShareHolderEntity singleShareHolderEntity = repository.findSingleShareHolderNode(parentModel.getCurrentQueryId());
        SingleShareHolderModel singleShareHolderModel = new SingleShareHolderModel().convertResult(singleShareHolderEntity);
        return parentModel.recordSingleShareHolder(singleShareHolderModel);
    }
}
