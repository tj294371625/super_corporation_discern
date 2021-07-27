package com.chinadaas.task.impl;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.utils.AssistantUtils;
import com.chinadaas.common.utils.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.model.SuperCorporationModel;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithmChain;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 链路识别
 * @createTime 2021.07.20
 */
@Slf4j
@Order(1)
@Component
public class ChainDiscernTask implements FullTask {

    private final Executor executor;
    private final SuperCorporationAlgorithmChain superCorporationAlgorithmChain;
    private final ChainOperationService chainOperationService;

    public ChainDiscernTask(@Qualifier("parallelExecutor") Executor executor,
                            SuperCorporationAlgorithmChain superCorporationAlgorithmChain,
                            ChainOperationService chainOperationService) {

        this.executor = executor;
        this.superCorporationAlgorithmChain = superCorporationAlgorithmChain;
        this.chainOperationService = chainOperationService;
    }

    @Override
    public void run() {
        log.info("chain discern task run start...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> chainDiscernTask = (entId) -> {

            SuperCorporationModel superCorporationModel = superCorporationAlgorithmChain.discernSpecialNode(entId, ModelType.PARENT);
            if (ModelStatus.NO_RESULT.equals(superCorporationModel.getResultStatus())) {
                return;
            }

            try {
                chainOperationService.parentChainPersistence(AssistantUtils.modelTransferToEntity(superCorporationModel));
            } catch (Exception e) {
                log.warn("chain persistence fail, entId: [{}]", entId);
            }

        };

        executor.execute("chain discern", chainDiscernTask);

        log.info("end the chain discern task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }

}
