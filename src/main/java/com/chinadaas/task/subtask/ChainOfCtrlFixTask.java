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
public class ChainOfCtrlFixTask {

    private final Executor parallelExecutor;
    private final ChainOperationService chainOperationService;

    @Autowired
    public ChainOfCtrlFixTask(@Qualifier("parallelExecutor") Executor parallelExecutor,
                              ChainOperationService chainOperationService) {

        this.parallelExecutor = parallelExecutor;
        this.chainOperationService = chainOperationService;
    }

    public void fixChainOfCtrl() {
        log.info("ChainOfCtrlFix task run start...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> chainOfCtrlFixTask = (entId) -> {

            try {
                chainOperationService.recursiveChainFix(entId, ModelType.FIN_CTRL);
            } catch (Exception e) {
                log.warn("execute ChainOfCtrlFix task fail, chain initial entId: [{}]", entId, e);
            }

        };

        parallelExecutor.execute("ChainOfCtrlFix", chainOfCtrlFixTask);

        log.info("end the ChainOfCtrlFix task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }
}