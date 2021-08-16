package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ImportMode;
import com.chinadaas.common.util.MongoHelper;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.io.EntIdListHolder;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.AbstractDiscernDataService;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 全量数据识别服务
 * @createTime 2021.07.02
 */
@Slf4j
@Service
public class DiscernFullDataServiceImpl extends AbstractDiscernDataService {

    private final MongoHelper mongoHelper;
    private final List<FullTask> fullTasks;


    @Autowired
    public DiscernFullDataServiceImpl(MongoHelper mongoHelper,
                                      List<FullTask> fullTasks,
                                      EntIdListLoader entIdListLoader,
                                      EntIdListHolder entIdListHolder,
                                      RecordHandler recordHandler) {

        this.mongoHelper = mongoHelper;
        this.fullTasks = fullTasks;
        this.entIdListLoader = entIdListLoader;
        this.entIdListHolder = entIdListHolder;
        this.recordHandler = recordHandler;
    }

    @Override
    protected void preDoDiscern() {
        mongoHelper.resetCollection();
    }

    @Override
    protected void doDiscern() {
        log.info("main task start run...");
        long startTime = TimeUtils.startTime();

        for (FullTask fullTask : fullTasks) {
            fullTask.run();
        }

        log.info("end the main task run, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }

    @Override
    public boolean hit(ImportMode mode) {
        return ImportMode.FULL_MODE.equals(mode);
    }

}
