package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.util.Assert;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.SnapshotHandler;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.repository.ChainOperationRepository;
import com.chinadaas.repository.NodeOperationRepository;
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
    private final ChainOperationRepository chainOperationRepository;
    private final NodeOperationRepository nodeOperationRepository;

    @Autowired
    public ChainOperationServiceImpl(RecordHandler recordHandler,
                                     ChainOperationRepository chainOperationRepository,
                                     NodeOperationRepository nodeOperationRepository) {

        this.recordHandler = recordHandler;
        this.chainOperationRepository = chainOperationRepository;
        this.nodeOperationRepository = nodeOperationRepository;
    }

    @Override
    public void recursiveChainFix(String entId, ModelType modelType) {
        ChainEntity chainEntity = chainOperationRepository.chainQuery(entId, modelType);
        Assert.nonNull(chainEntity, "ChainOperationService#recursiveChainFix " +
                "call ChainOperationRepository#chainQuery, entId: [{}] not found in mongodb.", entId);

        // 首次查询获得的结果无法穿透，则return
        if (parentIdUnknown(chainEntity)) {
            return;
        }

        SnapshotHandler snapshotHandler = SnapshotHandler.newInstance();
        snapshotHandler.circularCheck(entId);
        snapshotHandler.chainLengthAccum(chainEntity.getTemp2SourceLayer());

        recursiveChainFix(chainEntity, snapshotHandler, modelType);
    }

    @Override
    public void chainPersistence(ChainEntity chainEntity, ModelType modelType) {
        chainOperationRepository.chainPersistence(chainEntity, modelType);
    }

    @Override
    public void chainBatchDeleteOfParent(Set<String> entIds) {
        chainOperationRepository.chainBatchDeleteOfParent(entIds);
    }

    @Override
    public void chainBatchDeleteOfCtrl(Set<String> delEntIds) {
        chainOperationRepository.chainBatchDeleteOfCtrl(delEntIds);
    }

    @Override
    public Set<String> treeQuery(String entId, ModelType modelType) {
        Set<String> tempTreeResult = chainOperationRepository.treeQuery(entId, modelType);

        // 当前点无向下关联关系
        if (CollectionUtils.isEmpty(tempTreeResult)) {
            tempTreeResult.add(entId);
            return tempTreeResult;
        }

        Set<String> treeResult = Sets.newHashSet();
        treeResult.add(entId);
        for (String sourceEntId : tempTreeResult) {
            Set<String> recResults = this.treeQuery(sourceEntId, modelType);
            treeResult.addAll(recResults);
        }

        return treeResult;
    }

    @Override
    public ChainEntity chainQuery(String entId, ModelType modelType) {
        return chainOperationRepository.chainQuery(entId, modelType);
    }

    @Override
    public Set<String> obtainParentFixEntIds() {
        return chainOperationRepository.obtainParentFixEntIds();
    }

    @Override
    public Set<String> obtainFinCtrlEntIds() {
        return chainOperationRepository.obtainFinCtrlEntIds();
    }

    @Override
    public String obtainEntBeforeDisclosure(String sourceEntId, String targetEntId) {
        ChainEntity chainEntity = chainOperationRepository.chainQuery(sourceEntId, ModelType.PARENT);

        String tempEntId = chainEntity.getTempEntId();
        if (targetEntId.equals(tempEntId)) {
            return sourceEntId;
        }

        return obtainEntBeforeDisclosure(tempEntId, targetEntId);
    }

    @Override
    public Set<String> obtainFinCtrlFixEntIds() {
        return chainOperationRepository.obtainFinCtrlFixEntIds();
    }

    @Override
    public Set<String> obtainCircularEntIds() {
        return chainOperationRepository.obtainCircularEntIds();
    }

    private void recursiveChainFix(ChainEntity chainEntity, SnapshotHandler snapshotHandler, ModelType modelType) {

        // zs: 记录上一次实体
        ChainEntity preChainEntity = null;
        while (parentIdKnown(chainEntity)) {
            preChainEntity = chainEntity;
            chainEntity = chainOperationRepository.chainQuery(preChainEntity.getTempEntId(), modelType);

            // zs: 解决目标节点不存在于SC_CHAIN_*的问题（名单中未提供）
            if (Objects.isNull(chainEntity)) {
                doRecursiveChainFix(preChainEntity, snapshotHandler, modelType);
                return;
            }

            if (snapshotHandler.circularCheck(chainEntity.getSourceEntId())) {
                handleCircular(chainEntity, snapshotHandler, modelType);
                return;
            }

            snapshotHandler.chainLengthAccum(chainEntity.getTemp2SourceLayer());
        }

        doRecursiveChainFix(preChainEntity, snapshotHandler, modelType);
    }

    private void handleCircular(ChainEntity chainEntity, SnapshotHandler snapshotHandler, ModelType modelType) {
        resolveCircular(chainEntity, snapshotHandler, modelType);
        recordCircular(chainEntity, snapshotHandler);
    }

    private void recordCircular(ChainEntity chainEntity, SnapshotHandler snapshotHandler) {
        chainOperationRepository.saveCircularEntIds(snapshotHandler.obtainCircularEntIds(chainEntity.getSourceEntId()));

        log.warn("discover circular path, please see circular_record.csv detail.");
        recordHandler.recordCircular(snapshotHandler.obtainCircularPath());
    }

    private void resolveCircular(ChainEntity chainEntity, SnapshotHandler snapshotHandler, ModelType modelType) {
        ChainEntity preChainEntity;
        List<NodeWrapper> circularNodes = nodeOperationRepository
                .nodesFind(snapshotHandler.obtainCircularEntIds(chainEntity.getSourceEntId()));
        preChainEntity = snapshotHandler.resolveCircuit(circularNodes);
        doRecursiveChainFix(preChainEntity, snapshotHandler, modelType);
    }

    private void doRecursiveChainFix(ChainEntity preChainEntity, SnapshotHandler snapshotHandler, ModelType modelType) {
        String parentId = preChainEntity.getTempEntId();
        String parentName = preChainEntity.getTempName();
        String parentType = preChainEntity.getTargetType();
        String nodeEntId = snapshotHandler.obtainNodeEntId();
        long totalChainLength = snapshotHandler.obtainTotalChainLength();
        chainOperationRepository.chainFix(
                parentId,
                parentName,
                parentType,
                totalChainLength,
                nodeEntId,
                modelType
        );
    }

    private boolean parentIdUnknown(ChainEntity chainEntity) {
        return ChainConst.UNKNOWN_ID.equals(chainEntity.getTempEntId());
    }

    private boolean parentIdKnown(ChainEntity chainEntity) {
        return !parentIdUnknown(chainEntity);
    }
}
