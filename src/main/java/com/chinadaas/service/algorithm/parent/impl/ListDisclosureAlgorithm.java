package com.chinadaas.service.algorithm.parent.impl;

import com.chinadaas.entity.ListDisclosureEntity;
import com.chinadaas.model.ListDisclosureModel;
import com.chinadaas.model.ParentModel;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.algorithm.parent.ParentAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author liubc
 * @version 1.0.0
 * @description 上市披露算法
 * @createTime 2021.07.13
 */
@Order(4)
@Component
public class ListDisclosureAlgorithm implements ParentAlgorithm {

    private final NodeOperationRepository repository;

    @Autowired
    public ListDisclosureAlgorithm(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean calculation(ParentModel parentModel) {
        ListDisclosureEntity listDisclosureEntity = repository.findListDisclosureNode(parentModel.getCurrentQueryId());
        ListDisclosureModel listDisclosureModel = new ListDisclosureModel().convertResult(listDisclosureEntity);
        return parentModel.recordListDisclosureResult(listDisclosureModel);
    }
}
