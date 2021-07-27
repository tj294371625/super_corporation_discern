package com.chinadaas.task.impl;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.utils.AssistantUtils;
import com.chinadaas.common.utils.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.27
 */
@Slf4j
@Order(3)
@Component
public class FinCtrlDiscernTask implements FullTask {

    private final Executor executor;
    private final EntIdListLoader entIdListLoader;
    private final FinCtrlAlgorithmChain finCtrlAlgorithmChain;
    private final ChainOperationService chainOperationService;

    @Autowired
    public FinCtrlDiscernTask(@Qualifier("parallelExecutor") Executor executor,
                              EntIdListLoader entIdListLoader,
                              FinCtrlAlgorithmChain finCtrlAlgorithmChain,
                              ChainOperationService chainOperationService) {

        this.executor = executor;
        this.entIdListLoader = entIdListLoader;
        this.finCtrlAlgorithmChain = finCtrlAlgorithmChain;
        this.chainOperationService = chainOperationService;
    }

    @Override
    public void run() {
        log.info("finCtrl discern task run start...");
        long startTime = TimeUtils.startTime();

        // zs: 载入候选标识名单，用于向下穿透获取最终控股股东
        Set<String> queryFinCtrlEntIds = chainOperationService.queryFinCtrlEntIds();
        entIdListLoader.reloadEntIdList(queryFinCtrlEntIds);

        final Consumer<String> chainDiscernTask = (entId) -> {

            FinCtrlModel finCtrlModel = finCtrlAlgorithmChain.discernFinCtrlNode(entId);
            if (ModelStatus.NO_RESULT.equals(finCtrlModel.getResultStatus())) {
                return;
            }

            try {
                chainOperationService.finCtrlChainPersistence(AssistantUtils.modelTransferToEntity(finCtrlModel));
            } catch (Exception e) {
                log.warn("finCtrl chain persistence fail, entId: [{}]", entId);
            }

        };

        executor.execute("finCtrl discern", chainDiscernTask);

        log.info("end the finCtrl discern, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }
}
