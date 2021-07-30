package com.chinadaas.repository.impl;

import com.chinadaas.common.utils.Assert;
import com.chinadaas.entity.SuperCorporationEntity;
import com.chinadaas.repository.SuperCorporationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.29
 */
@Slf4j
@Repository
public class SuperCorporationRepositoryImpl implements SuperCorporationRepository {

    private static final Object LOCK = new Object();

    @Value("${db.mongodb.superCollection}")
    private String SC_SUPER_CORPORATION;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public SuperCorporationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void insertSuperCorporation(SuperCorporationEntity superCorporationEntity) {

        synchronized (LOCK) {
            int index = 0;
            while (index < 3) {
                try {
                    mongoTemplate.insert(superCorporationEntity, SC_SUPER_CORPORATION);
                    break;
                } catch (Exception e) {
                    index++;
                    log.warn("SuperCorporationRepositoryImpl#insertSuperCorporation insert fail, " +
                            "try insert count: [{}]", index, e);
                    Assert.isFalse(3 == index, "");
                }
            }
        }

    }
}
