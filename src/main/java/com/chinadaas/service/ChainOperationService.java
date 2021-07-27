package com.chinadaas.service;

import com.chinadaas.entity.ChainEntity;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 链路操作
 * @createTime 2021.07.21
 */
public interface ChainOperationService {

    /**
     * 递归修复链路
     *
     * @param entId
     */
    void recursiveChainFix(String entId);

    /**
     * 持久化链路（母公司）
     *
     * @param chainEntity
     */
    void parentChainPersistence(ChainEntity chainEntity);

    /**
     * 持久化链路（最终控股股东）
     *
     * @param chainEntity
     */
    void finCtrlChainPersistence(ChainEntity chainEntity);

    void chainBatchDelete(Set<String> entIds);

    /**
     * 查找变更树
     *
     * @param entId
     * @return
     */
    Set<String> treeQuery(String entId);

    /**
     * 链路查询
     *
     * @param entId
     * @return
     */
    ChainEntity chainQuery(String entId);

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
