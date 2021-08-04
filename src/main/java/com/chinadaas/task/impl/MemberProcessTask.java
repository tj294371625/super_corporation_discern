package com.chinadaas.task.impl;

import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.MemberService;
import com.chinadaas.service.SuperCorporationService;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 执行成员业务逻辑
 * @createTime 2021.08.03
 */
@Slf4j
@Order(3)
@Component
public class MemberProcessTask implements FullTask {

    private final RecordHandler recordHandler;
    private final EntIdListLoader entIdListLoader;
    private final SuperCorporationService superCorporationService;
    private final MemberService memberService;

    @Autowired
    public MemberProcessTask(RecordHandler recordHandler,
                             EntIdListLoader entIdListLoader,
                             SuperCorporationService superCorporationService,
                             MemberService memberService) {

        this.recordHandler = recordHandler;
        this.entIdListLoader = entIdListLoader;
        this.superCorporationService = superCorporationService;
        this.memberService = memberService;
    }

    private ParentIdExtraTask parentIdExtraTask;

    @PostConstruct
    public void init() {
        parentIdExtraTask = new ParentIdExtraTask();
    }

    @Override
    public void run() {
        parentIdExtraTask.run();

    }

    private class ParentIdExtraTask {

        public void run() {
            Set<String> parentIds = superCorporationService.extraParentIds();
            entIdListLoader.reloadEntIdList(parentIds);
        }
    }

    private class MemberCalTask {

    }
}
