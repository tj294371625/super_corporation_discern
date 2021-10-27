package com.chinadaas.task.impl;

import com.chinadaas.common.util.EntIdListOptHandler;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.service.SuperCorporationService;
import com.chinadaas.task.IncrTask;
import com.chinadaas.task.subtask.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.21
 */
@Slf4j
@Order(1)
@Component
public class IncrPreProcessTask implements IncrTask {

    private final RecordHandler recordHandler;
    private final EntIdListOptHandler entIdListOptHandler;
    private final IncrListScreenTask incrListScreenTask;
    private final IncrListDelTask incrListDelTask;
    private final IncrListRecordTask incrListRecordTask;
    private final ChainOfParentDiscernTask chainOfParentDiscernTask;
    private final ChainOfParentFixTask chainOfParentFixTask;
    private final ChainOfCtrlDiscernTask chainOfCtrlDiscernTask;
    private final ChainOfCtrlFixTask chainOfCtrlFixTask;
    private final SuperCorporationService superCorporationService;

    @Autowired
    public IncrPreProcessTask(RecordHandler recordHandler,
                              EntIdListOptHandler entIdListOptHandler,
                              IncrListScreenTask incrListScreenTask,
                              IncrListDelTask incrListDelTask,
                              IncrListRecordTask incrListRecordTask,
                              ChainOfParentDiscernTask chainOfParentDiscernTask,
                              ChainOfParentFixTask chainOfParentFixTask,
                              ChainOfCtrlDiscernTask chainOfCtrlDiscernTask,
                              ChainOfCtrlFixTask chainOfCtrlFixTask,
                              SuperCorporationService superCorporationService) {

        this.recordHandler = recordHandler;
        this.entIdListOptHandler = entIdListOptHandler;
        this.incrListScreenTask = incrListScreenTask;
        this.incrListDelTask = incrListDelTask;
        this.incrListRecordTask = incrListRecordTask;
        this.chainOfParentDiscernTask = chainOfParentDiscernTask;
        this.chainOfParentFixTask = chainOfParentFixTask;
        this.chainOfCtrlDiscernTask = chainOfCtrlDiscernTask;
        this.chainOfCtrlFixTask = chainOfCtrlFixTask;
        this.superCorporationService = superCorporationService;
    }

    @Override
    public void run() {

        incrDataPreProcess();

        discernChainOfParent();

        fixChainOfParent();

        discernChainOfCtrl();

        fixChainOfCtrl();

    }

    private void fixChainOfCtrl() {
        chainOfCtrlFixTask.fixChainOfCtrl();
    }

    private void discernChainOfCtrl() {
        chainOfCtrlDiscernTask.discernChainOfCtrl();
    }

    private void fixChainOfParent() {
        chainOfParentFixTask.fixChainOfParent();
    }

    private void discernChainOfParent() {
        entIdListOptHandler.replacePreWithIncrFixEntIds();
        chainOfParentDiscernTask.discernChainOfParent();
    }

    private void incrDataPreProcess() {
        incrListScreenTask.screenList();

        recordParentIds();

        entIdListOptHandler.replacePreWithIncrDelEntIds();
        incrListDelTask.delList();

        incrListRecordTask.recordList();
    }

    private void recordParentIds() {
        // zs: 获取待删除的parent，记录parentid，交给数据工厂
        Set<String> delTypeIncrSet = recordHandler.obtainDelTypeIncrSet();
        Set<String> delParentIds = superCorporationService.queryParentIdsByEntIds(delTypeIncrSet);
        recordHandler.recordDelListForMembers(delParentIds);

        // zs: 获取待新增的parent，记录parentid，重新生成成员
        Set<String> auTypeIncrSet = recordHandler.obtainAUTypeIncrSet();
        Set<String> auParentIds = superCorporationService.queryParentIdsByEntIds(auTypeIncrSet);
        recordHandler.recordParentIdIncr(auParentIds);
    }

}


