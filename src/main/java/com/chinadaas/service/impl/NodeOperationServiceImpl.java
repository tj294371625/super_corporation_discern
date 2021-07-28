package com.chinadaas.service.impl;

import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.NodeEntity;
import com.chinadaas.entity.TwoNodesEntity;
import com.chinadaas.repository.NodeOperationRepository;
import com.chinadaas.service.NodeOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lawliet
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
    public boolean managementStatus(String entId) {
        NodeEntity nodeEntity = repository.nodeFind(entId);
        NodeWrapper node = nodeEntity.getNode();
        String entStatus = node.getEntstatus();
        final String management = "1";
        return management.equals(entStatus);
    }

    @Override
    public List<TwoNodesEntity> sourceToTargetUseInv(String sourceId, String targetId, long targetToSourceLayer) {
        return repository.sourceToTargetUseInv(sourceId, targetId, targetToSourceLayer);
    }

    @Override
    public List<TwoNodesEntity> sourceToPersonUseInv(String sourceId, String personId, long personToSourceLayer) {
        return repository.sourceToPersonUseInv(sourceId, personId, personToSourceLayer);
    }

    @Override
    public LinkWrapper groupParentMappingTenInvMerge(long fromId, long toId) {
        return repository.groupParentMappingTenInvMerge(fromId, toId);
    }
}
