package com.chinadaas.repository.impl;

import com.chinadaas.common.constant.SuperConst;
import com.chinadaas.commons.factory.CypherBuilderFactory;
import com.chinadaas.component.mapper.base.Mapper;
import com.chinadaas.component.template.Neo4jTemplate;
import com.chinadaas.entity.*;
import com.chinadaas.repository.MemberRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author lawliet
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.08.03
 */
@SuppressWarnings("unchecked")
@Slf4j
@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private static final String NODE_ID = "nodeid";
    private static final String MORE_INFO = "moreinfo";
    private static final String STAFF_LIST_KEY = "staffList";
    public static final String ZS_ID = "zsid";
    public static final String ENT_ID = "entid";

    /**
     * 单位 second
     */
    private final long WAIT_TIME = 60L;

    // zs: 临时生成的表
    @Value("${db.mongodb.superCollection}")
    private String SC_SUPER_CORPORATION;

    private static final List<String> STAFF_LIST = Arrays.asList("410A", "410B", "410C", "410D", "410E", "410F", "410G", "410Z",
            "430A", "431A", "432K", "433A", "433B", "434Q", "436A", "441A", "441B", "441C", "441D", "441E", "441F", "441G", "442G",
            "451D", "490A", "491A");

    private final MongoTemplate mongoTemplate;
    private final Neo4jTemplate neo4jTemplate;
    private final Mapper mapper;

    @Autowired
    public MemberRepositoryImpl(MongoTemplate mongoTemplate,
                                Neo4jTemplate neo4jTemplate,
                                Mapper mapper) {

        this.mongoTemplate = mongoTemplate;
        this.neo4jTemplate = neo4jTemplate;
        this.mapper = mapper;
    }

    /**
     * 子公司历史信息
     *
     * @param parentId
     */
    @Override
    public List<MemberEntity> obtainMembers(String parentId) {
        Query condition = new Query(Criteria.where(SuperConst.PARENT_ID).is(parentId));
        List<Map> tempResultList = mongoTemplate.find(condition, Map.class, SC_SUPER_CORPORATION);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return Collections.EMPTY_LIST;
        }

        List<MemberEntity> results = Lists.newArrayList();
        for (Map tempResult : tempResultList) {
            MemberEntity memberEntity = mapper.getBean(tempResult, MemberEntity.class);
            results.add(memberEntity);
        }

        return results;
    }

    /**
     * 母公司及主要投资者个人共同控制企业
     *
     * @param parentId
     */
    @Override
    public List<DiscernAndMajorPersonEntity> obtainDiscernAndMajorPersons(String parentId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(NODE_ID, parentId);
        String cypher = CypherBuilderFactory
                .getCypherBuilder("cypher/discernAndMajorPersonQuery.cql").build();

        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return Collections.EMPTY_LIST;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<DiscernAndMajorPersonEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            DiscernAndMajorPersonEntity discernAndMajorPersonEntity
                    = mapper.getBean(info, DiscernAndMajorPersonEntity.class);
            results.add(discernAndMajorPersonEntity);
        }
        return results;
    }

    /**
     * 母公司及其关键管理人员共同控制的公司
     *
     * @param parentId
     */
    @Override
    public List<DiscernAndStaffEntity> obtainDiscernAndStaffs(String parentId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put(NODE_ID, parentId);
        params.put(STAFF_LIST_KEY, STAFF_LIST);

        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/discernAndStaffQuery.cql").build();
        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return Collections.EMPTY_LIST;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<DiscernAndStaffEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            DiscernAndStaffEntity discernAndStaffEntity = mapper.getBean(info, DiscernAndStaffEntity.class);
            results.add(discernAndStaffEntity);
        }
        return results;
    }

    /**
     * 母公司法人对外直接控制的公司
     *
     * @param parentId
     */
    @Override
    public DiscernLegalOutEntity obtainDiscernLegalOut(String parentId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put(NODE_ID, parentId);

        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/discernLegalOutQuery.cql").build();
        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return null;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<DiscernLegalOutEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            DiscernLegalOutEntity discernLegalEntity = mapper.getBean(info, DiscernLegalOutEntity.class);
            results.add(discernLegalEntity);
        }

        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0);
    }

    /**
     * 母公司最终控股自然人任职法人企业
     *
     * @param zsId
     * @param parentId
     */
    @Override
    public ControlPersonLegalEntity obtainControlPersonLegal(String zsId, String parentId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put(ZS_ID, zsId);
        params.put(ENT_ID, parentId);
        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/controlPersonLegalQuery.cql").build();

        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return null;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<ControlPersonLegalEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            ControlPersonLegalEntity controlPersonLegalEntity = mapper.getBean(info, ControlPersonLegalEntity.class);
            results.add(controlPersonLegalEntity);
        }

        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0);
    }

    /**
     * 母公司最终控股自然人对外控制的企业
     *
     * @param zsId
     * @param parentId
     */
    @Override
    public PersonOutControlEntity obtainPersonOutControl(String zsId, String parentId) {
        String FIN_CTRL_PROPERTY_INV_TYPE = "fin_ctrl_property.invtype";
        Query condition = new Query(
                Criteria.where(SuperConst.FIN_CTRL_ID).is(zsId)
                        .and(FIN_CTRL_PROPERTY_INV_TYPE).is("20")
                        .and(ENT_ID).ne(parentId)
        );

        List<Map> tempResultList = mongoTemplate.find(condition, Map.class, SC_SUPER_CORPORATION);
        PersonOutControlEntity entity = new PersonOutControlEntity();
        entity.setTempResultList(tempResultList);
        return entity;
    }

    /**
     * 母公司主要投资者个人直接对外控制公司
     *
     * @param parentId
     * @return
     */
    @Override
    public MajorPersonEntity obtainMajorPerson(String parentId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(NODE_ID, parentId);

        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/majorPersonQuery.cql").build();
        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return null;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<MajorPersonEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            MajorPersonEntity majorPersonEntity = mapper.getBean(info, MajorPersonEntity.class);
            results.add(majorPersonEntity);
        }

        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0);

    }

    /**
     * 母公司关键管理人员直接对外控制公司
     *
     * @param parentId
     * @return
     */
    @Override
    public StaffEntity obtainStaff(String parentId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(NODE_ID, parentId);
        params.put(STAFF_LIST_KEY, STAFF_LIST);

        String cypher = CypherBuilderFactory.getCypherBuilder("cypher/staffQuery.cql").build();
        List<Map<String, Object>> tempResultList = neo4jTemplate.executeCypher(cypher, params, WAIT_TIME);
        if (CollectionUtils.isEmpty(tempResultList)) {
            return null;
        }

        Map<String, Object> tempResult = tempResultList.get(0);
        List<Map<String, Object>> moreInfo = (List<Map<String, Object>>) tempResult.get(MORE_INFO);

        List<StaffEntity> results = Lists.newArrayList();
        for (Map<String, Object> info : moreInfo) {
            StaffEntity staffEntity = mapper.getBean(info, StaffEntity.class);
            results.add(staffEntity);
        }

        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0);
    }

    /**
     * 自然人控股企业通用查询
     *
     * @param zsId
     * @param parentId
     * @return
     */
    @Override
    public List<Map> commonPersonControlEnt(String zsId, String parentId) {
        Query condition = new Query(
                Criteria.where("fin_ctrl_id").is(zsId)
                        .and("parent_id").ne(parentId)
                        .and("entid").ne(parentId)
        );

        return mongoTemplate.find(condition, Map.class, SC_SUPER_CORPORATION);
    }

    /**
     * 获取最终控股自然人zsId
     *
     * @param parentId
     * @return
     */
    @Override
    public Map finalControlPerson(String parentId) {
        Query condition = new Query(
                Criteria.where("parent_id").is(parentId)
                        .and("fin_ctrl_property.invtype").is("20")
        );

        return mongoTemplate.findOne(condition, Map.class, SC_SUPER_CORPORATION);
    }
}
