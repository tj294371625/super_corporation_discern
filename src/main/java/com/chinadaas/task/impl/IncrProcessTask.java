package com.chinadaas.task.impl;

import com.chinadaas.task.IncrTask;
import com.chinadaas.task.subtask.MembersProcessTask;
import com.chinadaas.task.subtask.SuperCorporationProcessTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.08.16
 */
@Slf4j
@Component
public class IncrProcessTask implements IncrTask {

    private final SuperCorporationProcessTask superCorporationProcessTask;
    private final MembersProcessTask membersProcessTask;

    @Autowired
    public IncrProcessTask(SuperCorporationProcessTask superCorporationProcessTask,
                           MembersProcessTask membersProcessTask) {

        this.superCorporationProcessTask = superCorporationProcessTask;
        this.membersProcessTask = membersProcessTask;
    }

    @Override
    public void run() {
        generateSuperCorporation();
        generateMembers();
    }

    private void generateSuperCorporation() {
        this.superCorporationProcessTask.generateSuperCorporation();
    }

    private void generateMembers() {
        this.membersProcessTask.generateMembers();
    }
}
