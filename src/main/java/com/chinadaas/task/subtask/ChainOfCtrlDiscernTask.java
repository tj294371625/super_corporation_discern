package com.chinadaas.task.subtask;


import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.model.ChainModel;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithmChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class ChainOfCtrlDiscernTask {

    private final Executor parallelExecutor;
    private final SuperCorporationAlgorithmChain superCorporationAlgorithmChain;
    private final ChainOperationService chainOperationService;

    @Autowired
    public ChainOfCtrlDiscernTask(@Qualifier("parallelExecutor") Executor parallelExecutor,
                                  SuperCorporationAlgorithmChain superCorporationAlgorithmChain,
                                  ChainOperationService chainOperationService) {

        this.parallelExecutor = parallelExecutor;
        this.superCorporationAlgorithmChain = superCorporationAlgorithmChain;
        this.chainOperationService = chainOperationService;
    }

    public void discernChainOfCtrl() {
        log.info("ChainOfCtrlDiscern task start run...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> chainOfCtrlDiscernTask = (entId) -> {

            ChainModel chainModel = superCorporationAlgorithmChain.discernSpecifyTypeChain(entId, ModelType.FIN_CTRL);
            if (noResult(chainModel)) return;

            chainOperationService.chainPersistence(AssistantUtils.modelTransferToEntityOfChain(chainModel), ModelType.FIN_CTRL);
        };

        parallelExecutor.execute("ChainOfCtrlDiscern", chainOfCtrlDiscernTask);

        log.info("end the ChainOfCtrlDiscern task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }

    private boolean noResult(ChainModel chainModel) {
        return ModelStatus.NO_RESULT.equals(chainModel.getResultStatus());
    }
}