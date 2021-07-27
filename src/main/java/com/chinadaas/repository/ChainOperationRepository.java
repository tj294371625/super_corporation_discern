package com.chinadaas.repository;

import com.chinadaas.entity.ChainEntity;

import java.util.List;
import java.util.Set;

/**
 * @author lawliet
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
     * 链路持久化（母公司）
     *
     * @param chainEntity
     */
    void parentChainPersistence(ChainEntity chainEntity);

    /**
     * 链路持久化（母公司）
     *
     * @param chainEntity
     */
    void finCtrlChainPersistence(ChainEntity chainEntity);

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
     * 获取全部source entId
     *
     * @return
     */
    Set<String> fullSourceEntId();

    /**
     * 获取用于查询最终控股股东的候选entId
     *
     * @return
     */
    Set<String> queryFinCtrlEntIds();
}
