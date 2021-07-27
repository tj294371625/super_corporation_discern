package com.chinadaas.service.impl;

import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.NodeEntity;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.NodeOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubc
 * @version 1.0.0
 * @description
 * @createTime 2021.07.23
 */
@Slf4j
@Service
public class NodeOperationServiceImpl implements NodeOperationService {

    private final NodeOperationRepository repository;

    @Autowired
    public NodeOperationServiceImpl(NodeOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void nodeFix(String entId) {
        repository.nodeFix(entId);
    }

    @Override
    public boolean managementStatus(String entId) {
        NodeEntity nodeEntity = repository.nodeFind(entId);
        NodeWrapper node = nodeEntity.getNode();
        String entStatus = node.getEntstatus();
        final String management = "1";
        return management.equals(entStatus);
    }
}
