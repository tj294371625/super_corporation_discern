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
 * @description 链路修复任务
 * @createTime 2021.07.20
 */
@Slf4j
@Order(2)
@Component
public class ChainFixTask implements FullTask {

    private final Executor executor;
    private final EntIdListLoader entIdListLoader;
    private final ChainOperationService chainOperationService;

    @Autowired
    public ChainFixTask(Executor executor,
                        EntIdListLoader entIdListLoader,
                        ChainOperationService chainOperationService) {

        this.executor = executor;
        this.entIdListLoader = entIdListLoader;
        this.chainOperationService = chainOperationService;
    }

    @Override
    public void run() {
        log.info("chain fix task run start...");
        long startTime = TimeUtils.startTime();

        // zs: 替换企业标识名单，优化效率
        Set<String> fullEntIdList = chainOperationService.fullSourceEntId();
        entIdListLoader.reloadEntIdList(fullEntIdList);

        final Consumer<String> chainFixTask = (entId) -> {

            try {
                chainOperationService.recursiveChainFix(entId);
            } catch (Exception e) {
                log.warn("execute chain fix task fail, chain initial entId: [{}]", entId, e);
            }

        };

        executor.execute("chain fix", chainFixTask);

        log.info("end the chain fix task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }
}
