package com.chinadaas.task.impl;

import com.chinadaas.common.utils.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Order(4)
@Component
public class FinCtrlFixTask implements FullTask {

    private final Executor executor;
    private final ChainOperationService chainOperationService;

    @Autowired
    public FinCtrlFixTask(Executor executor,
                          ChainOperationService chainOperationService) {

        this.executor = executor;
        this.chainOperationService = chainOperationService;
    }

    @Override
    public void run() {
        log.info("finCtrl fix task run start...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> chainFixTask = (entId) -> {

            try {
                chainOperationService.recursiveChainFix(entId);
            } catch (Exception e) {
                log.warn("execute finCtrl fix task fail, chain initial entId: [{}]", entId, e);
            }

        };

        executor.execute("finCtrl fix", chainFixTask);

        log.info("end the finCtrl fix task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }
}
