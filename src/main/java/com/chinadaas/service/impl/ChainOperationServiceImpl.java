package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.utils.Assert;
import com.chinadaas.common.utils.SnapshotHandler;
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
import java.util.Objects;
import java.util.Set;

/**
 * @author lawliet
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
    public void recursiveChainFix(String entId, ModelType modelType) {
        ChainEntity chainEntity = repository.chainQuery(entId, modelType);
        Assert.nonNull(chainEntity, "ChainOperationService#recursiveChainFix " +
                "call ChainOperationRepository#chainQuery, entId: [{}] not found in mongodb.", entId);

        // 首次查询获得的结果无法穿透，则return
        if (parentIdUnknown(chainEntity)) {
            return;
        }

        SnapshotHandler snapshotHandler = SnapshotHandler.newInstance();
        snapshotHandler.circularCheck(entId);
        snapshotHandler.chainLengthAccum(chainEntity.getSource2TempLayer());

        recursiveChainFix(chainEntity, snapshotHandler, modelType);
    }

    @Override
    public void chainPersistence(ChainEntity chainEntity, ModelType modelType) {
        repository.chainPersistence(chainEntity, modelType);
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
    public ChainEntity chainQuery(String entId, ModelType modelType) {
        return repository.chainQuery(entId, modelType);
    }

    @Override
    public Set<String> fullSourceEntId() {
        return repository.fullSourceEntId();
    }

    @Override
    public Set<String> queryFinCtrlEntIds() {
        return repository.queryFinCtrlEntIds();
    }

    private void recursiveChainFix(ChainEntity chainEntity, SnapshotHandler snapshotHandler, ModelType modelType) {

        // zs: 记录上一次实体
        ChainEntity preChainEntity = null;
        while (parentIdKnown(chainEntity)) {
            preChainEntity = chainEntity;
            chainEntity = repository.chainQuery(preChainEntity.getTempEntId(), modelType);

            // zs: 解决目标节点不存在于SC_CHAIN_*的问题（名单中未提供）
            if (Objects.isNull(chainEntity)) {
                doRecursiveChainFix(preChainEntity, snapshotHandler, modelType);
                return;
            }

            if (snapshotHandler.circularCheck(chainEntity.getSourceEntId())) {
                log.warn("discover circular path, please see circular_record.csv detail.");
                recordHandler.recordCircular(snapshotHandler.obtainCircularPath());
                return;
            }

            snapshotHandler.chainLengthAccum(chainEntity.getSource2TempLayer());
        }

        doRecursiveChainFix(preChainEntity, snapshotHandler, modelType);
    }

    private void doRecursiveChainFix(ChainEntity preChainEntity, SnapshotHandler snapshotHandler, ModelType modelType) {
        String parentId = preChainEntity.getTempEntId();
        String parentName = preChainEntity.getTempName();
        String parentType = preChainEntity.getTargetType();
        List<String> chainEntIds = snapshotHandler.obtainChainEntIds(parentId);
        long totalChainLength = snapshotHandler.obtainTotalChainLength();
        repository.chainFix(
                parentId,
                parentName,
                parentType,
                totalChainLength,
                chainEntIds,
                modelType
        );
    }

    private boolean parentIdUnknown(ChainEntity chainEntity) {
        final String UNKNOWN_ID = "-1";
        return UNKNOWN_ID.equals(chainEntity.getTempEntId());
    }

    private boolean parentIdKnown(ChainEntity chainEntity) {
        return !parentIdUnknown(chainEntity);
    }
}
