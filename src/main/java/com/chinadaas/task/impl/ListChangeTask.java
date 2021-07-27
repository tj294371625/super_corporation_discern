package com.chinadaas.task.impl;

import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author liubc
 * @version 1.0.0
 * @description 名单变更任务
 * @createTime 2021.07.27
 */
@Slf4j
@Order(2)
@Component
public class ListChangeTask implements FullTask {

    private final ChainOperationService chainOperationService;
    private final EntIdListLoader entIdListLoader;

    @Autowired
    public ListChangeTask(ChainOperationService chainOperationService,
                          EntIdListLoader entIdListLoader) {

        this.chainOperationService = chainOperationService;
        this.entIdListLoader = entIdListLoader;
    }

    @Override
    public void run() {
        Set<String> changeList = chainOperationService.calNewEntIdList();
        entIdListLoader.reloadEntIdList(changeList);
    }
}
