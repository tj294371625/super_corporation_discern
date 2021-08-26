package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ImportMode;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.io.EntIdListHolder;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.AbstractDiscernDataService;
import com.chinadaas.task.FullTask;
import com.chinadaas.task.IncrTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 增量数据识别服务
 * @createTime 2021.07.20
 */
@Slf4j
@Service
public class DiscernIncrDataServiceImpl extends AbstractDiscernDataService {

    private final List<IncrTask> incrTasks;

    public DiscernIncrDataServiceImpl(List<IncrTask> incrTasks,
                                      EntIdListLoader entIdListLoader,
                                      EntIdListHolder entIdListHolder,
                                      RecordHandler recordHandler) {
        this.incrTasks = incrTasks;
        this.entIdListLoader = entIdListLoader;
        this.entIdListHolder = entIdListHolder;
        this.recordHandler = recordHandler;
    }

    @Override
    protected void doDiscern() {
        log.info("main task run start...");
        long startTime = TimeUtils.startTime();

        for (IncrTask incrTask : incrTasks) {
            incrTask.run();
        }

        log.info("end the main task run, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }

    @Override
    public boolean hit(ImportMode mode) {
        return ImportMode.INCR_MODE.equals(mode);
    }
}
