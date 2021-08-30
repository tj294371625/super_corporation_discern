package com.chinadaas.repository.impl;

import com.alibaba.fastjson.JSON;
import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.constant.SuperConst;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.repository.ChainOperationRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.20
 */
@Slf4j
@Repository
public class ChainOperationRepositoryImpl implements ChainOperationRepository {

    @Value("${db.mongodb.circularCollection}")
    private String SC_CHAIN_CIRCULAR;

    @Value("${db.mongodb.parentCollection}")
    private String SC_CHAIN_PARENT;

    @Value("${db.mongodb.finCtrlCollection}")
    private String SC_CHAIN_FINCTRL;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ChainOperationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ChainEntity chainQuery(String entId, ModelType modelType) {
        Query condition = new Query(Criteria.where(ChainConst.SOURCE_ENT_ID).is(entId));
        String collectionName = selectCollectionName(modelType);
        return mongoTemplate.findOne(condition, ChainEntity.class, collectionName);
    }

    @Override
    public List<ChainEntity> chainBatchQuery(Set<String> entIds) {
        List<List<String>> batchEntIds = batchProcess(entIds);

        List<ChainEntity> totalResults = Lists.newArrayList();
        for (List<String> batchEntId : batchEntIds) {
            Query condition = new Query(Criteria.where(ChainConst.SOURCE_ENT_ID).in(batchEntId));
            List<ChainEntity> tempResults = mongoTemplate.find(condition, ChainEntity.class, SC_CHAIN_PARENT);
            totalResults.addAll(tempResults);
        }

        return totalResults;
    }

    @Override
    public void chainFix(String parentId,
                         String parentName,
                         String parentType,
                         long totalChainLength,
                         String nodeEntId,
                         ModelType modelType) {

        Query matchCondition = new Query(Criteria.where(ChainConst.SOURCE_ENT_ID).is(nodeEntId));
        Update update = new Update()
                .set(ChainConst.TARGET_ENT_ID, parentId)
                .set(ChainConst.TARGET_NAME, parentName)
                .set(ChainConst.TARGET_TYPE, parentType)
                .set(ChainConst.TARGET_TO_SOURCE_LAYER, totalChainLength);
        String collectionName = selectCollectionName(modelType);
        mongoTemplate.updateMulti(matchCondition, update, collectionName);
    }

    @Override
    public void chainPersistence(ChainEntity chainEntity, ModelType modelType) {

        int index = 0;
        while (index < 3) {
            try {
                String collectionName = selectCollectionName(modelType);
                mongoTemplate.insert(chainEntity, collectionName);
                break;
            } catch (Exception e) {
                index++;
                TimeUtils.sleep(index, 50);
                log.warn(
                        "ChainOperationRepositoryImpl#chainPersistence insert fail, " +
                                "body: [{}], modelType: [{}], try insert count: [{}]",
                        JSON.toJSONString(chainEntity),
                        modelType.toString(),
                        index,
                        e
                );
            }
        }

    }

    @Override
    public void chainBatchDeleteOfParent(Set<String> entIds) {
        List<List<String>> batchEntIds = batchProcess(entIds);

        for (List<String> batchEntId : batchEntIds) {
            long startTime = TimeUtils.startTime();

            int index = 0;
            while (index < 3) {

                Query condition = new Query(Criteria.where(ChainConst.SOURCE_ENT_ID).in(batchEntId));

                try {
                    mongoTemplate.remove(condition, SC_CHAIN_PARENT);
                    break;
                } catch (Exception e) {
                    index++;
                    TimeUtils.sleep(index, 500);
                    log.warn(
                            "ChainOperationRepositoryImpl#chainBatchDeleteOfParent delete fail, " +
                                    "batch first entId: [{}], try delete count: [{}]",
                            batchEntId.get(0),
                            index,
                            e
                    );
                }

            }

            log.info("母公司链路表批量删除，耗费时间：[{}ms]", TimeUtils.endTime(startTime));
        }

    }


    @Override
    public void chainBatchDeleteOfCtrl(Set<String> delEntIds) {
        List<List<String>> batchEntIds = batchProcess(delEntIds);

        for (List<String> batchEntId : batchEntIds) {
            long startTime = TimeUtils.startTime();

            int index = 0;
            while (index < 3) {

                Query condition = new Query(Criteria.where(ChainConst.SOURCE_ENT_ID).in(batchEntId));

                try {
                    mongoTemplate.remove(condition, SC_CHAIN_FINCTRL);
                    break;
                } catch (Exception e) {
                    index++;
                    TimeUtils.sleep(index, 500);
                    log.warn(
                            "ChainOperationRepositoryImpl#chainBatchDeleteOfCtrl delete fail, " +
                                    "batch first entId: [{}], try delete count: [{}]",
                            batchEntId.get(0),
                            index,
                            e
                    );
                }

            }

            log.info("最终控股股东链路表批量删除，耗费时间：[{}ms]", TimeUtils.endTime(startTime));
        }
    }

    @Override
    public Set<String> treeQuery(String entId, ModelType modelType) {
        Set<String> treeResults = Sets.newHashSet();

        Query condition = new Query(Criteria.where(ChainConst.TARGET_ENT_ID).is(entId));
        condition.fields().exclude(ChainConst._ID).include(ChainConst.SOURCE_ENT_ID);

        String collectionName = selectCollectionName(modelType);
        List<Map> tempResults = mongoTemplate.find(condition, Map.class, collectionName);
        if (CollectionUtils.isEmpty(tempResults)) {
            return treeResults;
        }

        for (Map tempResult : tempResults) {
            String sourceEntId = (String) tempResult.get(ChainConst.SOURCE_ENT_ID);
            treeResults.add(sourceEntId);
        }
        return treeResults;
    }

    @Override
    public Set<String> obtainParentFixEntIds() {
        Set<String> parentFixEntIds = Sets.newHashSet();

        MongoCollection<Document> parentCollection = mongoTemplate.getCollection(SC_CHAIN_PARENT);
        parentCollection
                .find(
                        Filters.and(
                                Filters.ne(ChainConst.TARGET_ENT_ID, "-1")
                        )
                )
                .batchSize(50000)
                .projection(new Document(ChainConst._ID, 0).append(ChainConst.SOURCE_ENT_ID, 1))
                .forEach(
                        (Consumer<? super Document>) document -> {
                            if (Objects.nonNull(document)) {
                                parentFixEntIds.add(document.getString(ChainConst.SOURCE_ENT_ID));
                            }
                        }
                );

        return parentFixEntIds;
    }

    @Override
    public Set<String> obtainFinCtrlEntIds() {
        Set<String> finCtrlEntIds = Sets.newHashSet();

        MongoCollection<Document> parentCollection0 = mongoTemplate.getCollection(SC_CHAIN_PARENT);
        parentCollection0
                .find(
                        Filters.and(
                                Filters.eq(ChainConst.TARGET_ENT_ID, "-1")
                        )
                )
                .batchSize(50000)
                .projection(new Document(ChainConst._ID, 0).append(ChainConst.SOURCE_ENT_ID, 1))
                .forEach(
                        (Consumer<? super Document>) document -> {
                            if (Objects.nonNull(document)) {
                                finCtrlEntIds.add(document.getString(ChainConst.SOURCE_ENT_ID));
                            }
                        }
                );

        // zs: 修复母公司点不存在的情况
        MongoCollection<Document> parentCollection1 = mongoTemplate.getCollection(SC_CHAIN_PARENT);
        parentCollection1
                .find(
                        Filters.and(
                                Filters.ne(ChainConst.TARGET_ENT_ID, "-1")
                        )
                )
                .batchSize(50000)
                .projection(new Document(ChainConst._ID, 0).append(ChainConst.TARGET_ENT_ID, 1))
                .forEach(
                        (Consumer<? super Document>) document -> {
                            if (Objects.nonNull(document)) {
                                finCtrlEntIds.add(document.getString(ChainConst.TARGET_ENT_ID));
                            }
                        }
                );

        return finCtrlEntIds;
    }

    @Override
    public Set<String> obtainFinCtrlFixEntIds() {
        Set<String> finCtrlFixEntIds = Sets.newHashSet();

        MongoCollection<Document> parentCollection = mongoTemplate.getCollection(SC_CHAIN_FINCTRL);
        parentCollection
                .find(
                        Filters.and(
                                Filters.ne(ChainConst.TARGET_ENT_ID, "-1")
                        )
                )
                .batchSize(50000)
                .projection(new Document(ChainConst._ID, 0).append(ChainConst.SOURCE_ENT_ID, 1))
                .forEach(
                        (Consumer<? super Document>) document -> {
                            if (Objects.nonNull(document)) {
                                finCtrlFixEntIds.add(document.getString(ChainConst.SOURCE_ENT_ID));
                            }
                        }
                );

        return finCtrlFixEntIds;
    }

    @Override
    public void saveCircularEntIds(List<String> circularEntIds) {
        for (String circularEntId : circularEntIds) {
            Map<String, String> circularItem = Maps.newHashMap();
            circularItem.put(ChainConst.SOURCE_ENT_ID, circularEntId);
            mongoTemplate.insert(circularItem, SC_CHAIN_CIRCULAR);
        }
    }

    @Override
    public Set<String> obtainCircularEntIds() {
        List<Map> tempResults = mongoTemplate.findAll(Map.class, SC_CHAIN_CIRCULAR);

        Set<String> circularEntIds = Sets.newHashSet();
        for (Map tempResult : tempResults) {
            String sourceEntId = (String) tempResult.get(ChainConst.SOURCE_ENT_ID);
            circularEntIds.add(sourceEntId);
        }

        return circularEntIds;
    }

    private List<List<String>> batchProcess(Set<String> entIds) {
        final int BATCH_SIZE = 500;
        return Lists.partition(new ArrayList<>(entIds), BATCH_SIZE);
    }

    private String selectCollectionName(ModelType modelType) {
        // zs: 违反ocp
        if (ModelType.FIN_CTRL.equals(modelType)) {
            return SC_CHAIN_FINCTRL;
        } else if (ModelType.PARENT.equals(modelType)) {
            return SC_CHAIN_PARENT;
        } else {
            throw new IllegalArgumentException("input a illegal modelType");
        }
    }

}
