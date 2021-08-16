package com.chinadaas.task.impl;

import com.chinadaas.common.util.EntIdListOptHandler;
import com.chinadaas.task.FullTask;
import com.chinadaas.task.subtask.ChainOfCtrlDiscernTask;
import com.chinadaas.task.subtask.ChainOfCtrlFixTask;
import com.chinadaas.task.subtask.ChainOfParentDiscernTask;
import com.chinadaas.task.subtask.ChainOfParentFixTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 前置处理全量数据
 * @createTime 2021.07.27
 */
@Slf4j
@Order(1)
@Component
public class FullPreProcessTask implements FullTask {

    private final EntIdListOptHandler entIdListOptHandler;
    private final ChainOfParentDiscernTask chainOfParentDiscernTask;
    private final ChainOfParentFixTask chainOfParentFixTask;
    private final ChainOfCtrlDiscernTask chainOfCtrlDiscernTask;
    private final ChainOfCtrlFixTask chainOfCtrlFixTask;

    @Autowired
    public FullPreProcessTask(EntIdListOptHandler entIdListOptHandler,
                              ChainOfParentDiscernTask chainOfParentDiscernTask,
                              ChainOfParentFixTask chainOfParentFixTask,
                              ChainOfCtrlDiscernTask chainOfCtrlDiscernTask,
                              ChainOfCtrlFixTask chainOfCtrlFixTask) {

        this.entIdListOptHandler = entIdListOptHandler;
        this.chainOfParentDiscernTask = chainOfParentDiscernTask;
        this.chainOfParentFixTask = chainOfParentFixTask;
        this.chainOfCtrlDiscernTask = chainOfCtrlDiscernTask;
        this.chainOfCtrlFixTask = chainOfCtrlFixTask;
    }

    @Override
    public void run() {

        discernChainOfParent();

        fixChainOfParent();

        discernChainOfCtrl();

        fixChainOfCtrl();

    }

    private void fixChainOfCtrl() {
        this.entIdListOptHandler.replacePreWithFinCtrlFixEntIds();
        this.chainOfCtrlFixTask.fixChainOfCtrl();
    }

    private void discernChainOfCtrl() {
        this.entIdListOptHandler.replacePreWithFinCtrlEntIds();
        this.chainOfCtrlDiscernTask.discernChainOfCtrl();
    }

    private void fixChainOfParent() {
        this.entIdListOptHandler.replacePreWithParentFixEntIds();
        this.chainOfParentFixTask.fixChainOfParent();
    }

    private void discernChainOfParent() {
        this.chainOfParentDiscernTask.discernChainOfParent();
    }

}