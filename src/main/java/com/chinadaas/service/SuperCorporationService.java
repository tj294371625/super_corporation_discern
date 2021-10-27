package com.chinadaas.service;

import com.chinadaas.entity.SuperCorporationEntity;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团服务
 * @createTime 2021.07.29
 */
public interface SuperCorporationService {

    void insertSuperCorporation(SuperCorporationEntity superCorporationEntity);

    Set<String> extraParentIds();

    void superBatchDelete(Set<String> entIds);

    Set<String> queryParentIdsByEntIds(Set<String> entIds);
}
