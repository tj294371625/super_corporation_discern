package com.chinadaas.task.impl;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.model.ChainModel;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.algorithm.SuperCorporationAlgorithmChain;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 前置处理全量数据
 * @createTime 2021.07.27
 */
@Slf4j
@Order(1)
@Component
public class PreProcessTask implements FullTask {

    private final Executor singleExecutor;
    private final Executor parallelExecutor;
    private final EntIdListLoader entIdListLoader;
    private final SuperCorporationAlgorithmChain superCorporationAlgorithmChain;
    private final ChainOperationService chainOperationService;

    @Autowired
    public PreProcessTask(Executor singleExecutor,
                          @Qualifier("parallelExecutor") Executor parallelExecutor,
                          EntIdListLoader entIdListLoader,
                          SuperCorporationAlgorithmChain superCorporationAlgorithmChain,
                          ChainOperationService chainOperationService) {

        this.singleExecutor = singleExecutor;
        this.parallelExecutor = parallelExecutor;
        this.entIdListLoader = entIdListLoader;
        this.superCorporationAlgorithmChain = superCorporationAlgorithmChain;
        this.chainOperationService = chainOperationService;
    }

    private ChainOfParentDiscernTask chainOfParentDiscernTask;
    private ChainOfParentFixTask chainOfParentFixTask;
    private ChainOfCtrlDiscernTask chainOfCtrlDiscernTask;
    private ChainOfCtrlFixTask chainOfCtrlFixTask;


    @PostConstruct
    public void init() {
        this.chainOfParentDiscernTask = new ChainOfParentDiscernTask();
        this.chainOfParentFixTask = new ChainOfParentFixTask();
        this.chainOfCtrlDiscernTask = new ChainOfCtrlDiscernTask();
        this.chainOfCtrlFixTask = new ChainOfCtrlFixTask();
    }

    @Override
    public void run() {
        chainOfParentDiscernTask.run();
        chainOfParentFixTask.run();
        chainOfCtrlDiscernTask.run();
        chainOfCtrlFixTask.run();
    }

    private class ChainOfParentDiscernTask {

        public void run() {
            log.info("chainOfParentDiscern task start run...");
            long startTime = TimeUtils.startTime();

            final Consumer<String> chainDiscernTask = (entId) -> {

                ChainModel chainModel = superCorporationAlgorithmChain.discernSpecialTypeChain(entId, ModelType.PARENT);
                if (ModelStatus.NO_RESULT.equals(chainModel.getResultStatus())) {
                    return;
                }

                try {
                    chainOperationService.chainPersistence(AssistantUtils.modelTransferToEntityOfChain(chainModel), ModelType.PARENT);
                } catch (Exception e) {
                    log.warn("chainPersistence fail, entId: [{}]", entId);
                }

            };

            parallelExecutor.execute("chainOfParentDiscern", chainDiscernTask);

            log.info("end the chainOfParentDiscern task, spend time: [{}ms]", TimeUtils.endTime(startTime));
        }
    }

    private class ChainOfParentFixTask {

        public void run() {
            log.info("ChainOfParentFix task start run...");
            long startTime = TimeUtils.startTime();

            // zs: 替换企业标识名单，优化效率
            Set<String> fullEntIdList = chainOperationService.fullSourceEntId();
            entIdListLoader.reloadEntIdList(fullEntIdList);

            final Consumer<String> chainFixTask = (entId) -> {

                try {
                    chainOperationService.recursiveChainFix(entId, ModelType.PARENT);
                } catch (Exception e) {
                    log.warn("execute ChainOfParentFix task fail, chain initial entId: [{}]", entId, e);
                }

            };

            singleExecutor.execute("ChainOfParentFix", chainFixTask);

            log.info("end the ChainOfParentFix task, spend time: [{}ms]", TimeUtils.endTime(startTime));
        }
    }

    private class ChainOfCtrlDiscernTask {

        public void run() {
            log.info("ChainOfCtrlDiscern task start run...");
            long startTime = TimeUtils.startTime();

            // zs: 载入候选标识名单，用于向下穿透获取最终控股股东
            Set<String> queryFinCtrlEntIds = chainOperationService.queryFinCtrlEntIds();
            entIdListLoader.reloadEntIdList(queryFinCtrlEntIds);

            final Consumer<String> chainDiscernTask = (entId) -> {

                ChainModel chainModel
                        = superCorporationAlgorithmChain.discernSpecialTypeChain(entId, ModelType.FIN_CTRL);
                if (ModelStatus.NO_RESULT.equals(chainModel.getResultStatus())) {
                    return;
                }

                try {
                    chainOperationService.chainPersistence(AssistantUtils.modelTransferToEntityOfChain(chainModel), ModelType.FIN_CTRL);
                } catch (Exception e) {
                    log.warn("chainPersistence fail, entId: [{}]", entId);
                }

            };

            parallelExecutor.execute("ChainOfCtrlDiscern", chainDiscernTask);

            log.info("end the ChainOfCtrlDiscern task, spend time: [{}ms]", TimeUtils.endTime(startTime));
        }
    }

    private class ChainOfCtrlFixTask {

        public void run() {
            log.info("ChainOfCtrlFix task run start...");
            long startTime = TimeUtils.startTime();

            final Consumer<String> chainFixTask = (entId) -> {

                try {
                    chainOperationService.recursiveChainFix(entId, ModelType.FIN_CTRL);
                } catch (Exception e) {
                    log.warn("execute ChainOfCtrlFix task fail, chain initial entId: [{}]", entId, e);
                }

            };

            singleExecutor.execute("ChainOfCtrlFix", chainFixTask);

            log.info("end the ChainOfCtrlFix task, spend time: [{}ms]", TimeUtils.endTime(startTime));
        }
    }

}
