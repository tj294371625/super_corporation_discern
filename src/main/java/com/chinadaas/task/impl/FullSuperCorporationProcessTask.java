package com.chinadaas.task.impl;

import com.chinadaas.common.util.EntIdListOptHandler;
import com.chinadaas.task.FullTask;
import com.chinadaas.task.subtask.SuperCorporationProcessTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.28
 */
@Slf4j
@Order(2)
@Component
public class FullSuperCorporationProcessTask implements FullTask {

    private final EntIdListOptHandler entIdListOptHandler;
    private final SuperCorporationProcessTask superCorporationProcessTask;

    @Autowired
    public FullSuperCorporationProcessTask(EntIdListOptHandler entIdListOptHandler,
                                           SuperCorporationProcessTask superCorporationProcessTask) {

        this.entIdListOptHandler = entIdListOptHandler;
        this.superCorporationProcessTask = superCorporationProcessTask;
    }

    @Override
    public void run() {
        generateSuperCorporation();
    }

    private void generateSuperCorporation() {
        this.entIdListOptHandler.replacePreWithSuperCorEntIds();
        this.superCorporationProcessTask.generateSuperCorporation();
    }
}
