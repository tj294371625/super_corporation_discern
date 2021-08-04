package com.chinadaas.task.impl;

import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.task.IncrTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 增量名单删除
 * @createTime 2021.07.23
 */
@Slf4j
@Order(2)
@Component
public class IncrListDelTask implements IncrTask {

    private final RecordHandler recordHandler;
    private final ChainOperationService chainOperationService;

    @Autowired
    public IncrListDelTask(RecordHandler recordHandler,
                           ChainOperationService chainOperationService) {

        this.recordHandler = recordHandler;
        this.chainOperationService = chainOperationService;
    }

    @Override
    public void run() {
        log.info("incrList delete task run start...");
        long startTime = TimeUtils.startTime();

        Set<String> delTypeIncr = recordHandler.delTypeIncrSet();
        // 清空链路表中更新树和非在营点的信息
        chainOperationService.chainBatchDelete(delTypeIncr);

        log.info("end the incrList delete task, spend time: [{}ms]", TimeUtils.endTime(startTime));

    }
}
