package com.chinadaas.repository;

import com.chinadaas.entity.SuperCorporationEntity;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.07.29
 */
public interface SuperCorporationRepository {

    void insertSuperCorporation(SuperCorporationEntity superCorporationEntity);

    Set<String> extraParentIds();

    void superBatchDelete(Set<String> delTypeIncr);

    Set<String> queryParentIdsByEntIds(Set<String> entIds);

}
