package com.chinadaas.service.impl;

import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ImportMode;
import com.chinadaas.common.constant.SuperConst;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.io.EntIdListHolder;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.AbstractDiscernDataService;
import com.chinadaas.task.FullTask;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexModel;
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

    @Value("${db.mongodb.superCollection}")
    private String SC_SUPER_CORPORATION;

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

        BasicDBObject sourceEntIdIndex = new BasicDBObject();
        sourceEntIdIndex.put(ChainConst.SOURCE_ENT_ID, 1);
        BasicDBObject tempEntIdIndex = new BasicDBObject();
        tempEntIdIndex.put(ChainConst.TEMP_ENT_ID, 1);
        BasicDBObject targetEntIdIndex = new BasicDBObject();
        targetEntIdIndex.put(ChainConst.TARGET_ENT_ID, 1);

        List<IndexModel> chainIndexModels = Lists.newArrayList();
        chainIndexModels.add(new IndexModel(sourceEntIdIndex));
        chainIndexModels.add(new IndexModel(tempEntIdIndex));
        chainIndexModels.add(new IndexModel(targetEntIdIndex));

        mongoTemplate.createCollection(SC_CHAIN_PARENT).createIndexes(chainIndexModels);
        mongoTemplate.createCollection(SC_CHAIN_FINCTRL).createIndexes(chainIndexModels);

        if (mongoTemplate.collectionExists(SC_SUPER_CORPORATION)) {
            mongoTemplate.dropCollection(SC_SUPER_CORPORATION);
        }

        BasicDBObject entIdIndex = new BasicDBObject();
        entIdIndex.put(SuperConst.ENT_ID, 1);
        BasicDBObject finCtrlIdIndex = new BasicDBObject();
        finCtrlIdIndex.put(SuperConst.FIN_CTRL_ID, 1);
        BasicDBObject parentIdIndex = new BasicDBObject();
        parentIdIndex.put(SuperConst.PARENT_ID, 1);
        BasicDBObject entNameIndex = new BasicDBObject();
        entNameIndex.put(SuperConst.ENT_NAME, 1);
        BasicDBObject parentNameIndex = new BasicDBObject();
        parentNameIndex.put(SuperConst.PARENT_NAME, 1);

        List<IndexModel> superIndexModels = Lists.newArrayList();
        superIndexModels.add(new IndexModel(entIdIndex));
        superIndexModels.add(new IndexModel(finCtrlIdIndex));
        superIndexModels.add(new IndexModel(parentIdIndex));
        superIndexModels.add(new IndexModel(entNameIndex));
        superIndexModels.add(new IndexModel(parentNameIndex));

        mongoTemplate.createCollection(SC_SUPER_CORPORATION).createIndexes(superIndexModels);
    }
}
