package com.chinadaas.task.subtask;

import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.SuperCorporationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 增量名单删除
 * @createTime 2021.07.23
 */
@Slf4j
@Component
public class IncrListDelTask {

    private final Executor parallelExecutor;
    private final ChainOperationService chainOperationService;
    private final SuperCorporationService superCorporationService;

    @Autowired
    public IncrListDelTask(@Qualifier("parallelExecutor") Executor parallelExecutor,
                           ChainOperationService chainOperationService,
                           SuperCorporationService superCorporationService) {

        this.parallelExecutor = parallelExecutor;
        this.chainOperationService = chainOperationService;
        this.superCorporationService = superCorporationService;
    }

    public void delList() {
        log.info("incrList delete task run start...");
        long startTime = TimeUtils.startTime();


        final Consumer<Set<String>> incrDelTask = (delEntIds) -> {

            // zs: 清空链路表、集团表中更新树的非在营点信息
            CompletableFuture<Void> future = CompletableFuture.allOf(
                    CompletableFuture.runAsync(
                            () -> chainOperationService.chainBatchDeleteOfParent(delEntIds)
                    ),
                    CompletableFuture.runAsync(
                            () -> chainOperationService.chainBatchDeleteOfCtrl(delEntIds)
                    ),
                    CompletableFuture.runAsync(
                            () -> superCorporationService.superBatchDelete(delEntIds)
                    )
            );

            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        };

        parallelExecutor.executeTasks("incrDelTask", incrDelTask);

        log.info("end the incrList delete task, spend time: [{}ms]", TimeUtils.endTime(startTime));

    }
}
