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
import java.util.Objects;

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
        if (Objects.isNull(nodeEntity)) {
            return false;
        }
        NodeWrapper node = nodeEntity.getNode();
        String entStatus = node.obtainEntstatus();
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
    public List<TwoNodesEntity> sourceToTargetUseGroupParent(String sourceId, String targetId) {
        return repository.sourceToTargetUseGroupParent(sourceId, targetId);
    }

    @Override
    public LinkWrapper groupParentMappingTenInvMerge(long fromId, long toId) {
        return repository.groupParentMappingTenInvMerge(fromId, toId);
    }
}
