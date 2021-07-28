package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ImportMode;
import com.chinadaas.common.utils.RecordHandler;
import com.chinadaas.common.utils.TimeUtils;
import com.chinadaas.component.io.EntIdListHolder;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.AbstractDiscernDataService;
import com.chinadaas.task.FullTask;
import com.chinadaas.task.Task;
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

    @Value("${db.mongodb.parentCollection}")
    private String SC_CHAIN_PARENT;

    @Value("${db.mongodb.finCtrlCollection}")
    private String SC_CHAIN_FINCTRL;

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
    protected void preDoDiscern() {
        resetCollection();
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

    private void resetCollection() {
        if (mongoTemplate.collectionExists(SC_CHAIN_PARENT)) {
            mongoTemplate.dropCollection(SC_CHAIN_PARENT);
        }

        if (mongoTemplate.collectionExists(SC_CHAIN_FINCTRL)) {
            mongoTemplate.dropCollection(SC_CHAIN_FINCTRL);
        }

        DBObject sourceEntIdIndex = new BasicDBObject();
        sourceEntIdIndex.put(ChainConst.SOURCE_ENT_ID, 1);

        DBObject tempEntIdIndex = new BasicDBObject();
        tempEntIdIndex.put(ChainConst.TEMP_ENT_ID, 1);

        DBObject targetEntIdIndex = new BasicDBObject();
        targetEntIdIndex.put(ChainConst.TARGET_ENT_ID, 1);

        mongoTemplate.createCollection(SC_CHAIN_PARENT).createIndex(sourceEntIdIndex);
        mongoTemplate.createCollection(SC_CHAIN_PARENT).createIndex(tempEntIdIndex);
        mongoTemplate.createCollection(SC_CHAIN_PARENT).createIndex(targetEntIdIndex);

        mongoTemplate.createCollection(SC_CHAIN_FINCTRL).createIndex(sourceEntIdIndex);
        mongoTemplate.createCollection(SC_CHAIN_FINCTRL).createIndex(tempEntIdIndex);
        mongoTemplate.createCollection(SC_CHAIN_FINCTRL).createIndex(targetEntIdIndex);
    }
}
