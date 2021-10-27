package com.chinadaas.task.impl;

import com.chinadaas.common.util.EntIdListOptHandler;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.service.SuperCorporationService;
import com.chinadaas.task.IncrTask;
import com.chinadaas.task.subtask.MembersProcessTask;
import com.chinadaas.task.subtask.SuperCorporationProcessTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.08.16
 */
@Slf4j
@Order(2)
@Component
public class IncrProcessTask implements IncrTask {

    private final RecordHandler recordHandler;
    private final EntIdListOptHandler entIdListOptHandler;
    private final SuperCorporationProcessTask superCorporationProcessTask;
    private final MembersProcessTask membersProcessTask;
    private final SuperCorporationService superCorporationService;

    @Autowired
    public IncrProcessTask(RecordHandler recordHandler,
                           EntIdListOptHandler entIdListOptHandler,
                           SuperCorporationProcessTask superCorporationProcessTask,
                           MembersProcessTask membersProcessTask,
                           SuperCorporationService superCorporationService) {

        this.recordHandler = recordHandler;
        this.entIdListOptHandler = entIdListOptHandler;
        this.superCorporationProcessTask = superCorporationProcessTask;
        this.membersProcessTask = membersProcessTask;
        this.superCorporationService = superCorporationService;
    }

    @Override
    public void run() {
        generateSuperCorporation();
        recordParentDel();
        generateMembers();
    }

    private void generateSuperCorporation() {
        this.superCorporationProcessTask.generateSuperCorporation();
    }

    private void recordParentDel() {
        // zs: 获取待删除的parent，记录parentid，交给数据工厂
        Set<String> delTypeIncrSet = recordHandler.obtainDelTypeIncrSet();
        Set<String> delParentIds = superCorporationService.queryParentIdsByEntIds(delTypeIncrSet);
        recordHandler.recordDelListForMembers(delParentIds);
    }

    private void generateMembers() {
        this.entIdListOptHandler.replacePreWithAUIncrEntIds();
        this.membersProcessTask.generateMembers();
    }
}
