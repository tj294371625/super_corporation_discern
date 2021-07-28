package com.chinadaas.repository.impl;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.common.utils.Neo4jResultParseUtils;
import com.chinadaas.common.utils.RecordHandler;
import com.chinadaas.common.utils.TimeUtils;
import com.chinadaas.commons.exception.QueryNeo4jTimeOutException;
import com.chinadaas.commons.factory.CypherBuilderFactory;
import com.chinadaas.component.mapper.base.Mapper;
import com.chinadaas.component.template.Neo4jTemplate;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.entity.*;
import com.chinadaas.repository.NodeOperationRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.20
 */
@SuppressWarnings("unchecked")
@Slf4j
@Component
public class NodeOperationRepositoryImpl implements NodeOperationRepository {

    private static final String ENT_ID = "entId";
    private static final String MORE_INFO = "moreinfo";
    private static final String INV_ID = "inv_id";
    private static final String START_ID = "startId";
    private static final String END_ID = "endId";
    private static final String LAYER = "layer";

    /**
     * 单位 second
     */
    private final long WAIT_TIME = 60L;

    @Value("${db.mongodb.singleCollection}")
    private String MODEL_PARENT_SINGLE;
    @Value("${db.mongodb.parentCollection}")
    private String SC_CHAIN_PARENT;

    private final RecordHandler recordHandler;
    private final Neo4jTemplate neo4jTemplate;
    private final MongoTemplate mongoTemplate;
    private final Mapper mapper;

    @Autowired
    public NodeOperationRepositoryImpl(RecordHandler recordHandler,
                                       Neo4jTemplate neo4jTemplate,
                                       MongoTemplate mongoTemplate,
                                       Mapper mapper) {

        this.recordHandler = recordHandler;
        this.neo4jTemplate = neo4jTemplate;
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
    }


    @Override
    public SourceEntity findSourceNode(String entId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(ENT_ID, entId);

        final String CYPHER_PATH = "cypher/findSourceNode.cql";
        String cypher = CypherBuilderFactory.getCypherBuilder(CYPHER_PATH).build();

        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);
        if (CollectionUtils.isEmpty(moreInfo)) {
            return null;
        }

        Map<String, Object> info = moreInfo.get(0);
        return mapper.getBean(info, SourceEntity.class);
    }

    @Override
    public List<DecisionEntity> findDecisionNode(String entId, ModelType modelType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(ENT_ID, entId);

        String cypher = createCypherWithSpecialType(modelType);

        List<Map<String, Object>> tempResultList = Lists.newArrayList();
        try {
            long startTime = TimeUtils.startTime();

            tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);

            long spendTime = TimeUtils.endTime(startTime);
            if (spendTime > 100L) {
                log.warn("NodeOperationRepositoryImpl#findDecisionNode method " +
                        "entId: [{}], slow query spend time: [{}ms]", entId, spendTime);
            }
        } catch (QueryNeo4jTimeOutException e) {
            recordHandler.recordTimeOut(entId);
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<DecisionEntity> decisionEntities = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            DecisionEntity decisionEntity = mapper.getBean(info, DecisionEntity.class);
            decisionEntities.add(decisionEntity);
        }
        return decisionEntities;
    }

    @Override
    public SingleShareHolderEntity findSingleShareHolderNode(String entId) {
        Query condition = new Query(Criteria.where("entid").is(entId));
        Map tempMapResult = mongoTemplate.findOne(condition, Map.class, MODEL_PARENT_SINGLE);
        if (CollectionUtils.isEmpty(tempMapResult)) {
            return null;
        }

        String singleShareHolderId = (String) tempMapResult.get(INV_ID);

        Map<String, Object> params = Maps.newHashMap();
        params.put(ENT_ID, singleShareHolderId);

        final String CYPHRE_PATH = "cypher/findSourceNode.cql";
        String cypher = CypherBuilderFactory.getCypherBuilder(CYPHRE_PATH).build();

        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);
        if (CollectionUtils.isEmpty(moreInfo)) {
            return null;
        }

        Map<String, Object> info = moreInfo.get(0);
        return mapper.getBean(info, SingleShareHolderEntity.class);
    }

    @Override
    public ListDisclosureEntity findListDisclosureNode(String entId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(ENT_ID, entId);

        final String CYPHER_PATH = "cypher/findListDisclosureNode.cql";
        String cypher = CypherBuilderFactory.getCypherBuilder(CYPHER_PATH).build();

        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);
        if (CollectionUtils.isEmpty(moreInfo)) {
            return null;
        }

        Map<String, Object> info = moreInfo.get(0);
        return mapper.getBean(info, ListDisclosureEntity.class);
    }

    @Override
    public NodeEntity nodeFind(String entId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(ENT_ID, entId);

        final String CYPHER_PATH = "cypher/findNode.cql";
        String cypher = CypherBuilderFactory.getCypherBuilder(CYPHER_PATH).build();

        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);
        if (CollectionUtils.isEmpty(moreInfo)) {
            return null;
        }

        Map<String, Object> info = moreInfo.get(0);
        return mapper.getBean(info, NodeEntity.class);
    }

    @Override
    public List<TwoNodesEntity> sourceToTargetUseInv(String sourceId, String targetId, long targetToSourceLayer) {
        Map<String, Object> param = Maps.newHashMap();
        param.put(START_ID, sourceId);
        param.put(END_ID, targetId);
        if (targetToSourceLayer < 8) {
            targetToSourceLayer = 8;
        }
        param.put(LAYER, targetToSourceLayer);

        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/sourceToTargetUseInvQuery.cql").build();
        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, param, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return Collections.EMPTY_LIST;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<TwoNodesEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            TwoNodesEntity twoNodesEntity = mapper.getBean(info, TwoNodesEntity.class);
            results.add(twoNodesEntity);
        }
        return results;
    }

    @Override
    public List<TwoNodesEntity> sourceToPersonUseInv(String sourceId, String personId, long personToSourceLayer) {
        Map<String, Object> param = Maps.newHashMap();
        param.put(START_ID, sourceId);
        param.put(END_ID, personId);
        param.put(LAYER, personToSourceLayer);

        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/sourceToPersonUseInvQuery.cql").build();
        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, param, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return Collections.EMPTY_LIST;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<TwoNodesEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            TwoNodesEntity twoNodesEntity = mapper.getBean(info, TwoNodesEntity.class);
            results.add(twoNodesEntity);
        }
        return results;
    }

    @Override
    public LinkWrapper groupParentMappingTenInvMerge(long fromId, long toId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("from", fromId);
        params.put("to", toId);
        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/groupParentMappingTenInvMergeQuery.cql").build();
        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return null;
        }

        Map<String, Object> linkMap = tempResultList.get(0);
        if (CollectionUtils.isEmpty(linkMap)) {
            return null;
        }

        Relationship relationship = (Relationship) linkMap.get("link");
        return Neo4jResultParseUtils.parseRelation(relationship);
    }

    private String createCypherWithSpecialType(ModelType modelType) {

        final String CYPHER_PATH;

        // zs: 违反ocp
        if (ModelType.FIN_CTRL.equals(modelType)) {
            CYPHER_PATH = "cypher/findDecisionNodeOfFinCtrl.cql";
        } else if (ModelType.PARENT.equals(modelType)) {
            CYPHER_PATH = "cypher/findDecisionNodeOfParent.cql";
        } else {
            throw new IllegalArgumentException("input a illegal modelType");
        }

        return CypherBuilderFactory.getCypherBuilder(CYPHER_PATH).build();
    }

}
