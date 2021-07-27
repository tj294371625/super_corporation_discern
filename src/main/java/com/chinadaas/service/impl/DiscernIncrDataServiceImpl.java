package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ImportMode;
import com.chinadaas.common.utils.RecordHandler;
import com.chinadaas.common.utils.TimeUtils;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.AbstractDiscernDataService;
import com.chinadaas.task.FullTask;
import com.chinadaas.task.IncrTask;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author liubc
 * @version 1.0.0
 * @description 增量数据识别服务
 * @createTime 2021.07.20
 */
@Slf4j
@Service
public class DiscernIncrDataServiceImpl extends AbstractDiscernDataService {

    private final List<IncrTask> incrTasks;
    private final List<FullTask> fullTasks;

    public DiscernIncrDataServiceImpl(List<IncrTask> incrTasks,
                                      List<FullTask> fullTasks,
                                      EntIdListLoader entIdListLoader,
                                      RecordHandler recordHandler) {
        this.incrTasks = incrTasks;
        this.fullTasks = fullTasks;
        this.entIdListLoader = entIdListLoader;
        this.recordHandler = recordHandler;
    }

    @Override
    protected void doDiscern() {
        log.info("main task run start...");
        long startTime = TimeUtils.startTime();

        for (IncrTask incrTask : incrTasks) {
            incrTask.run();
        }

        Set<String> auTypeIncrSet = recordHandler.auTypeIncrSet();
        if (CollectionUtils.isEmpty(auTypeIncrSet)) {
            log.info("input incrList no change, end the main task");
            return;
        }

        // 记录修正的变更名单
        Set<String> fixIncrList = Sets.newHashSet();
        fixIncrList.addAll(auTypeIncrSet);
        fixIncrList.addAll(recordHandler.delTypeIncrSet());
        for (String entId : fixIncrList) {
            recordHandler.recordIncrList(entId);
        }

        // 重新载入add&update类型变更名单
        entIdListLoader.reloadEntIdList(auTypeIncrSet);

        for (FullTask fullTask : fullTasks) {
            fullTask.run();
        }

        log.info("end the main task run, incr data size: [{}], spend time: [{}ms]",
                fixIncrList.size(), TimeUtils.endTime(startTime));
    }

    @Override
    public boolean hit(ImportMode mode) {
        return ImportMode.INCR_MODE.equals(mode);
    }
}
