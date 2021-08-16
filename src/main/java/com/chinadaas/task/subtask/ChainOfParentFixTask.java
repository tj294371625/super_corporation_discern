package com.chinadaas.task.subtask;


import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.service.ChainOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class ChainOfParentFixTask {

    private final Executor parallelExecutor;
    private final ChainOperationService chainOperationService;

    @Autowired
    public ChainOfParentFixTask(@Qualifier("parallelExecutor") Executor parallelExecutor,
                                ChainOperationService chainOperationService) {

        this.parallelExecutor = parallelExecutor;
        this.chainOperationService = chainOperationService;
    }

    public void fixChainOfParent() {
        log.info("ChainOfParentFix task start run...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> chainOfParentFixTask = (entId) -> {

            try {
                chainOperationService.recursiveChainFix(entId, ModelType.PARENT);
            } catch (Exception e) {
                log.warn("execute ChainOfParentFix task fail, chain initial entId: [{}]", entId, e);
            }

        };

        parallelExecutor.execute("ChainOfParentFix", chainOfParentFixTask);

        log.info("end the ChainOfParentFix task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }
}