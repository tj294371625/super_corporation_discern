package com.chinadaas.task.impl;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.model.ChainModel;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.NodeOperationService;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithmChain;
import com.chinadaas.task.IncrTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 增量名单筛选任务
 * @createTime 2021.07.21
 */
@Slf4j
@Order(1)
@Component
public class IncrListScreenTask implements IncrTask {

    private final Executor executor;
    private final RecordHandler recordHandler;
    private final SuperCorporationAlgorithmChain superCorporationAlgorithmChain;
    private final ChainOperationService chainOperationService;
    private final NodeOperationService nodeOperationService;

    @Autowired
    public IncrListScreenTask(@Qualifier("parallelExecutor") Executor executor,
                              RecordHandler recordHandler,
                              SuperCorporationAlgorithmChain superCorporationAlgorithmChain,
                              ChainOperationService chainOperationService,
                              NodeOperationService nodeOperationService) {

        this.executor = executor;
        this.recordHandler = recordHandler;
        this.superCorporationAlgorithmChain = superCorporationAlgorithmChain;
        this.chainOperationService = chainOperationService;
        this.nodeOperationService = nodeOperationService;
    }


    @Override
    public void run() {
        log.info("incrList screen task run start...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> incrListScreenTask = (entId) -> {
            // 1 判断企业是否在营
            boolean managementStatus = nodeOperationService.managementStatus(entId);
            // 非在营，记录
            if (!managementStatus) {
                recordHandler.recordDelTypeIncr(Collections.singletonList(entId));
                return;
            }

            // 2 判断当前在营企业是否发生了股权变化
            ChainModel chainModel
                    = superCorporationAlgorithmChain.discernSpecialTypeChain(entId, ModelType.PARENT);
            ChainEntity chainEntity = chainOperationService.chainQuery(entId, ModelType.PARENT);

            // 2.1 如果链路表中不存在该在营企业，则说明是新增的点，记录
            if (Objects.isNull(chainEntity)) {
                recordHandler.recordAUTypeIncr(Collections.singletonList(entId));
                return;
            }

            // 2.2 neo4j及链路表中存在该在营企业
            // 2.2.1 未发生变化，返回
            if (notChange(chainEntity, chainModel)) {
                return;
            }

            // 2.2.2 发生变化，记录
            Set<String> subIncrList = chainOperationService.treeQuery(entId);
            recordHandler.recordAUTypeIncr(subIncrList);
            recordHandler.recordDelTypeIncr(subIncrList);
        };

        executor.execute("incrList screen", incrListScreenTask);

        log.info("end the incrList screen task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }

    /**
     * true表示未发生变化
     *
     * @param chainEntity
     * @param chainModel
     * @return
     */
    private boolean notChange(ChainEntity chainEntity, ChainModel chainModel) {
        String targetEntId = chainEntity.getTempEntId();
        NodeWrapper parentNode = chainModel.getTargetNode();

        if (ModelStatus.SOURCE_ONLY.equals(chainModel.getResultStatus())) {
            final String UNKNOWN_ID = "-1";
            return UNKNOWN_ID.equals(targetEntId);
        }

        return targetEntId.equals(parentNode.obtainEntId());
    }
}


