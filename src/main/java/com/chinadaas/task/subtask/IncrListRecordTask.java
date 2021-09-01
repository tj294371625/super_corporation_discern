package com.chinadaas.task.subtask;

import com.chinadaas.common.util.RecordHandler;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.08.16
 */
@Slf4j
@Component
public class IncrListRecordTask {

    private final RecordHandler recordHandler;

    @Autowired
    public IncrListRecordTask(RecordHandler recordHandler) {
        this.recordHandler = recordHandler;
    }

    public void recordList() {
        // 记录修正的变更名单
        Set<String> fixIncrList = Sets.newHashSet();
        fixIncrList.addAll(recordHandler.obtainAUTypeIncrSet());
//        fixIncrList.addAll(recordHandler.obtainDelTypeIncrSet());
        for (String entId : fixIncrList) {
            recordHandler.recordIncrList(entId);
        }
    }
}
