package com.chinadaas.repository;

import com.chinadaas.entity.ChainEntity;

import java.util.List;
import java.util.Set;

/**
 * @author liubc
 * @version 1.0.0
 * @description 链路操作
 * @createTime 2021.07.20
 */
public interface ChainOperationRepository {

    /**
     * 链路查询
     *
     * @param entId
     * @return
     */
    ChainEntity chainQuery(String entId);

    /**
     * 链路批量查询
     *
     * @param entIds
     * @return
     */
    List<ChainEntity> chainBatchQuery(Set<String> entIds);

    /**
     * 链路修复
     *
     * @param parentId
     * @param parentName
     * @param sourceEntIds
     */
    void chainFix(String parentId, String parentName, List<String> sourceEntIds);

    /**
     * 链路持久化
     *
     * @param chainEntity
     */
    void chainPersistence(ChainEntity chainEntity);

    /**
     * 链路批量删除
     *
     * @param entIds
     */
    void chainBatchDelete(Set<String> entIds);

    /**
     * 变更树查询
     *
     * @param entId
     * @return
     */
    Set<String> treeQuery(String entId);

    /**
     * 获取新企业entId名单
     *
     * @return
     */
    Set<String> calNewEntIdList();
}
