package com.chinadaas.common.util;

import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.SuperConst;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.08.16
 */
@Slf4j
@Component
public class MongoHelper {

    @Value("${db.mongodb.circularCollection}")
    private String SC_CHAIN_CIRCULAR;

    @Value("${db.mongodb.parentCollection}")
    private String SC_CHAIN_PARENT;

    @Value("${db.mongodb.finCtrlCollection}")
    private String SC_CHAIN_FINCTRL;

    @Value("${db.mongodb.superCollection}")
    private String SC_SUPER_CORPORATION;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoHelper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void resetCollection() {

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



        if (mongoTemplate.collectionExists(SC_CHAIN_CIRCULAR)) {
            mongoTemplate.dropCollection(SC_CHAIN_CIRCULAR);
        }

        BasicDBObject circularIndex = new BasicDBObject();
        circularIndex.put(ChainConst.SOURCE_ENT_ID, 1);

        List<IndexModel> circularIndexModels = Lists.newArrayList();
        circularIndexModels.add(new IndexModel(circularIndex));

        mongoTemplate.createCollection(SC_CHAIN_CIRCULAR).createIndexes(circularIndexModels);




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
