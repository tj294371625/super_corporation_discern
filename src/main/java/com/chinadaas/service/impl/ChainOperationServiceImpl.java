package com.chinadaas.service.impl;

import com.chinadaas.common.utils.Assert;
import com.chinadaas.common.utils.CircularPathHandler;
import com.chinadaas.common.utils.RecordHandler;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.repository.ChainOperationRepository;
import com.chinadaas.service.ChainOperationService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author liubc
 * @version 1.0.0
 * @description
 * @createTime 2021.07.21
 */
@Slf4j
@Service
public class ChainOperationServiceImpl implements ChainOperationService {

    private final RecordHandler recordHandler;
    private final ChainOperationRepository repository;

    @Autowired
    public ChainOperationServiceImpl(RecordHandler recordHandler,
                                     ChainOperationRepository repository) {
        this.recordHandler = recordHandler;
        this.repository = repository;
    }

    @Override
    public void recursiveChainFix(String entId) {
        ChainEntity chainEntity = repository.chainQuery(entId);
        Assert.nonNull(chainEntity, "ChainFixServiceImpl#recursiveFix " +
                "call ChainFixRepository#recursiveQueryParent, entId: [{}] not found in mongodb.", entId);

        // 首次查询获得的结果无法穿透，则return
        if (parentIdUnknown(chainEntity)) {
            return;
        }

        CircularPathHandler snapshotHandler = CircularPathHandler.newInstance();
        snapshotHandler.circularCheck(entId);

        recursiveChainFix(chainEntity, snapshotHandler);
    }

    @Override
    public void chainPersistence(ChainEntity chainEntity) {
        repository.chainPersistence(chainEntity);
    }

    @Override
    public void chainBatchDelete(Set<String> entIds) {
        repository.chainBatchDelete(entIds);
    }

    @Override
    public Set<String> treeQuery(String entId) {
        Set<String> tempTreeResult = repository.treeQuery(entId);

        // 当前点无向下关联关系
        if (CollectionUtils.isEmpty(tempTreeResult)) {
            tempTreeResult.add(entId);
            return tempTreeResult;
        }

        Set<String> treeResult = Sets.newHashSet();
        treeResult.add(entId);
        for (String sourceEntId : tempTreeResult) {
            Set<String> recResults = this.treeQuery(sourceEntId);
            treeResult.addAll(recResults);
        }

        return treeResult;
    }

    @Override
    public ChainEntity chainQuery(String entId) {
        return repository.chainQuery(entId);
    }

    @Override
    public Set<String> calNewEntIdList() {
        return repository.calNewEntIdList();
    }

    private void recursiveChainFix(ChainEntity chainEntity, CircularPathHandler snapshotHandler) {

        while (parentIdNotUnknown(chainEntity)) {
            chainEntity = repository.chainQuery(chainEntity.getTargetEntId());
            if (snapshotHandler.circularCheck(chainEntity.getSourceEntId())) {
                log.warn("discover circular path, please see circular_record.csv detail.");
                recordHandler.recordCircular(snapshotHandler.obtainCircularPath());
                return;
            }
        }

        List<String> chainEntIds = snapshotHandler.obtainChain(chainEntity.getSourceEntId());
        doRecursiveChainFix(chainEntity, chainEntIds);
    }

    private void doRecursiveChainFix(ChainEntity parentEntity, List<String> sourceEntIds) {
        repository.chainFix(
                parentEntity.getSourceEntId(),
                parentEntity.getSourceName(),
                sourceEntIds
        );
    }

    private boolean parentIdUnknown(ChainEntity chainEntity) {
        final String UNKNOWN_ID = "-1";
        return UNKNOWN_ID.equals(chainEntity.getTargetEntId());
    }

    private boolean parentIdNotUnknown(ChainEntity chainEntity) {
        return !parentIdUnknown(chainEntity);
    }
}
