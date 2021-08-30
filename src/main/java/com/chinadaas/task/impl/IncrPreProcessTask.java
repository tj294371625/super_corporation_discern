package com.chinadaas.task.impl;

import com.chinadaas.common.util.EntIdListOptHandler;
import com.chinadaas.task.IncrTask;
import com.chinadaas.task.subtask.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

    private final EntIdListOptHandler entIdListOptHandler;
    private final IncrListScreenTask incrListScreenTask;
    private final IncrListDelTask incrListDelTask;
    private final IncrListRecordTask incrListRecordTask;
    private final ChainOfParentDiscernTask chainOfParentDiscernTask;
    private final ChainOfParentFixTask chainOfParentFixTask;
    private final ChainOfCtrlDiscernTask chainOfCtrlDiscernTask;
    private final ChainOfCtrlFixTask chainOfCtrlFixTask;

    @Autowired
    public IncrPreProcessTask(EntIdListOptHandler entIdListOptHandler,
                              IncrListScreenTask incrListScreenTask,
                              IncrListDelTask incrListDelTask,
                              IncrListRecordTask incrListRecordTask,
                              ChainOfParentDiscernTask chainOfParentDiscernTask,
                              ChainOfParentFixTask chainOfParentFixTask,
                              ChainOfCtrlDiscernTask chainOfCtrlDiscernTask,
                              ChainOfCtrlFixTask chainOfCtrlFixTask) {

        this.entIdListOptHandler = entIdListOptHandler;
        this.incrListScreenTask = incrListScreenTask;
        this.incrListDelTask = incrListDelTask;
        this.incrListRecordTask = incrListRecordTask;
        this.chainOfParentDiscernTask = chainOfParentDiscernTask;
        this.chainOfParentFixTask = chainOfParentFixTask;
        this.chainOfCtrlDiscernTask = chainOfCtrlDiscernTask;
        this.chainOfCtrlFixTask = chainOfCtrlFixTask;
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

        entIdListOptHandler.replacePreWithIncrDelEntIds();
        incrListDelTask.delList();

        incrListRecordTask.recordList();
    }

}


