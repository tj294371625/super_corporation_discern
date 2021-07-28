package com.chinadaas.repository.impl;

import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.utils.Assert;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.repository.ChainOperationRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.20
 */
@Slf4j
@Repository
public class ChainOperationRepositoryImpl implements ChainOperationRepository {

    private static final Object LOCK = new Object();

    /*private static final String ID = "_id";
    private static final String SOURCE_ENT_ID = "source_entid";
    private static final String TARGET_ENT_ID = "target_entid";
    private static final String GROUP_ENT_ID = "group_entid";
    private static final String GROUP_NAME = "group_name";*/

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
    public void chainFix(String parentId, String parentName, String parentType, long totalChainLength, List<String> sourceEntIds, ModelType modelType) {
        Query matchCondition = new Query(Criteria.where(ChainConst.SOURCE_ENT_ID).in(sourceEntIds));
        Update update = new Update()
                .set(ChainConst.TARGET_ENT_ID, parentId)
                .set(ChainConst.TARGET_NAME, parentName)
                .set(ChainConst.TARGET_TYPE, parentType)
                .set(ChainConst.SOURCE_TO_TARGET_LAYER, totalChainLength);
        String collectionName = selectCollectionName(modelType);
        mongoTemplate.updateMulti(matchCondition, update, collectionName);
    }

    @Override
    public void chainPersistence(ChainEntity chainEntity, ModelType modelType) {

        synchronized (LOCK) {
            int index = 0;
            while (index < 3) {
                try {
                    String collectionName = selectCollectionName(modelType);
                    mongoTemplate.insert(chainEntity, collectionName);
                    break;
                } catch (Exception e) {
                    index++;
                    log.warn("ChainOperationRepositoryImpl#chainPersistence insert fail, " +
                            "try insert count: [{}]", index, e);
                    Assert.isFalse(3 == index, "");
                }
            }
        }

    }

    @Override
    public void chainBatchDelete(Set<String> entIds) {
        List<List<String>> batchEntIds = batchProcess(entIds);

        for (List<String> batchEntId : batchEntIds) {
            Query condition = new Query(Criteria.where(ChainConst.SOURCE_ENT_ID).in(batchEntId));

            try {
                mongoTemplate.remove(condition, SC_CHAIN_PARENT);
            } catch (Exception e) {
                log.warn("ChainOperationRepositoryImpl#chainBatchDelete delete fail", e);
            }
        }

    }

    @Override
    public Set<String> treeQuery(String entId) {
        Set<String> treeResults = Sets.newHashSet();

        Query condition = new Query(Criteria.where(ChainConst.TARGET_ENT_ID).is(entId));
        condition.fields().exclude(ChainConst._ID).include(ChainConst.SOURCE_ENT_ID);
        List<Map> tempResults = mongoTemplate.find(condition, Map.class, SC_CHAIN_PARENT);
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
    public Set<String> fullSourceEntId() {
        Query condition = new Query();
        condition.fields().exclude(ChainConst._ID).include(ChainConst.SOURCE_ENT_ID);
        List<ChainEntity> allRecords = mongoTemplate.find(condition, ChainEntity.class, SC_CHAIN_PARENT);

        return allRecords.stream()
                .map(ChainEntity::getSourceEntId)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> queryFinCtrlEntIds() {
        Query condition = new Query(Criteria.where(ChainConst.TARGET_ENT_ID).is("-1"));
        condition.fields().exclude(ChainConst._ID).include(ChainConst.SOURCE_ENT_ID);
        List<ChainEntity> partRecords = mongoTemplate.find(condition, ChainEntity.class, SC_CHAIN_PARENT);

        return partRecords.stream()
                .map(ChainEntity::getSourceEntId)
                .collect(Collectors.toSet());
    }

    private List<List<String>> batchProcess(Set<String> entIds) {
        final int BATCH_SIZE = 1_000;
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
