package com.chinadaas.task.subtask;

import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.SuperCorporationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 增量名单删除
 * @createTime 2021.07.23
 */
@Slf4j
@Component
public class IncrListDelTask {

    private final RecordHandler recordHandler;
    private final ChainOperationService chainOperationService;
    private final SuperCorporationService superCorporationService;

    @Autowired
    public IncrListDelTask(RecordHandler recordHandler,
                           ChainOperationService chainOperationService,
                           SuperCorporationService superCorporationService) {

        this.recordHandler = recordHandler;
        this.chainOperationService = chainOperationService;
        this.superCorporationService = superCorporationService;
    }

    public void delList() {
        log.info("incrList delete task run start...");
        long startTime = TimeUtils.startTime();

        Set<String> delTypeIncr = recordHandler.obtainDelTypeIncrSet();
        // zs: 清空链路表、集团表中更新树和非在营点的信息
        chainOperationService.chainBatchDelete(delTypeIncr);
        superCorporationService.superBatchDelete(delTypeIncr);

        log.info("end the incrList delete task, spend time: [{}ms]", TimeUtils.endTime(startTime));

    }
}
