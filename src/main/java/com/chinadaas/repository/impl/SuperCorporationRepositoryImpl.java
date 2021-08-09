package com.chinadaas.repository.impl;

import com.alibaba.fastjson.JSON;
import com.chinadaas.common.constant.SuperConst;
import com.chinadaas.common.util.Assert;
import com.chinadaas.entity.SuperCorporationEntity;
import com.chinadaas.repository.SuperCorporationRepository;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.29
 */
@Slf4j
@Repository
public class SuperCorporationRepositoryImpl implements SuperCorporationRepository {

    @Value("${db.mongodb.superCollection}")
    private String SC_SUPER_CORPORATION;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public SuperCorporationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void insertSuperCorporation(SuperCorporationEntity superCorporationEntity) {

        doInsertSuperCorporation(superCorporationEntity);

    }

    @Override
    public Set<String> extraParentIds() {
        Set<String> parentIds = Sets.newHashSet();

        MongoCollection<Document> collection = mongoTemplate.getCollection(SC_SUPER_CORPORATION);
        collection
                .find(Filters.and(
                        Filters.ne(SuperConst.PARENT_ID, null)
                ))
                .batchSize(50000)
                .projection(new Document(SuperConst._ID, 0).append(SuperConst.PARENT_ID, 1))
                .forEach(
                        (Consumer<? super Document>) document -> {
                            if (Objects.nonNull(document)) {
                                parentIds.add(document.getString(SuperConst.PARENT_ID));
                            }
                        }
                );

        return parentIds;
    }

    private void doInsertSuperCorporation(SuperCorporationEntity superCorporationEntity) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(SC_SUPER_CORPORATION);

        Document document = new Document();
        document.append(SuperConst._ID, superCorporationEntity.getEntId())
                .append(SuperConst.ENT_ID, superCorporationEntity.getEntId())
                .append(SuperConst.ENT_NAME, superCorporationEntity.getEntName())
                .append(SuperConst.SOURCE_PROPERTY, superCorporationEntity.getSourceProperty())
                .append(SuperConst.FIN_CTRL_ID, superCorporationEntity.getFinCtrlId())
                .append(SuperConst.FIN_CTRL_NAME, superCorporationEntity.getFinCtrlName())
                .append(SuperConst.FIN_CTRL_PROPERTY, superCorporationEntity.getFinCtrlProperty())
                .append(SuperConst.PARENT_2_SOURCE_RELATION, superCorporationEntity.getParent2SourceRelation())
                .append(SuperConst.PARENT_ID, superCorporationEntity.getParentId())
                .append(SuperConst.PARENT_NAME, superCorporationEntity.getParentName())
                .append(SuperConst.PARENT_REG_NO, superCorporationEntity.getParentRegno())
                .append(SuperConst.PARENT_CREDIT_CODE, superCorporationEntity.getParentCreditcode())
                .append(SuperConst.CTRL_2_PARENT_PATH, JSON.toJSON(superCorporationEntity.getCtrl2ParentPath()))
                .append(SuperConst.PARENT_2_SOURCE_PATH, JSON.toJSON(superCorporationEntity.getParent2SourcePath()))
                .append(SuperConst.CTRL_2_SOURCE_PATH, JSON.toJSON(superCorporationEntity.getCtrl2SourcePath()))
                .append(SuperConst.PARENT_PROPERTY, superCorporationEntity.getParentProperty())
                .append(SuperConst.PARENT_2_SOURCE_CGZB, superCorporationEntity.getParent2SourceCgzb())
                .append(SuperConst.CTRL_2_SOURCE_CGZB, superCorporationEntity.getCtrl2SourceCgzb())
                .append(SuperConst.CTRL_2_PARENT_CGZB, superCorporationEntity.getCtrl2ParentCgzb())
                .append(SuperConst.EMID, superCorporationEntity.getEmId());

        collection.insertOne(document);
    }

}
