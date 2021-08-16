package com.chinadaas.task.impl;

import com.chinadaas.common.util.EntIdListOptHandler;
import com.chinadaas.task.FullTask;
import com.chinadaas.task.subtask.MembersProcessTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 执行成员业务逻辑
 * @createTime 2021.08.03
 */
@Slf4j
@Order(3)
@Component
public class FullMemberProcessTask implements FullTask {

    private final EntIdListOptHandler entIdListOptHandler;
    private final MembersProcessTask membersProcessTask;

    @Autowired
    public FullMemberProcessTask(EntIdListOptHandler entIdListOptHandler,
                                 MembersProcessTask membersProcessTask) {

        this.entIdListOptHandler = entIdListOptHandler;
        this.membersProcessTask = membersProcessTask;
    }

    @Override
    public void run() {
        generateMember();
    }

    private void generateMember() {
        entIdListOptHandler.replacePreWithMembersEntIds();
        membersProcessTask.generateMembers();
    }

}
