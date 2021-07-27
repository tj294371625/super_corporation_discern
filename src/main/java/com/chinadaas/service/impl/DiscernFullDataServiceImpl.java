package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ImportMode;
import com.chinadaas.common.utils.RecordHandler;
import com.chinadaas.common.utils.TimeUtils;
import com.chinadaas.component.io.EntIdListHolder;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.AbstractDiscernDataService;
import com.chinadaas.task.FullTask;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    private static final String SOURCE_ENT_ID = "source_entid";

    @Value("${db.mongodb.parentCollection}")
    private String SC_CHAIN_PARENT;

    private final List<FullTask> fullTasks;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DiscernFullDataServiceImpl(List<FullTask> fullTasks,
                                      EntIdListLoader entIdListLoader,
                                      EntIdListHolder entIdListHolder,
                                      RecordHandler recordHandler,
                                      MongoTemplate mongoTemplate) {

        this.fullTasks = fullTasks;
        this.entIdListLoader = entIdListLoader;
        this.entIdListHolder = entIdListHolder;
        this.recordHandler = recordHandler;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected void init() {
        // 全量跑数之前，清空SC_CHAIN表
        if (mongoTemplate.collectionExists(SC_CHAIN_PARENT)) {
            mongoTemplate.dropCollection(SC_CHAIN_PARENT);
        }

        DBObject bdo = new BasicDBObject();
        bdo.put(SOURCE_ENT_ID, 1);
        mongoTemplate.createCollection(SC_CHAIN_PARENT).createIndex(bdo);
    }

    @Override
    protected void doDiscern() {
        log.info("main task run start...");
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
