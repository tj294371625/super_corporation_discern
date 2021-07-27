package com.chinadaas.service;

import com.chinadaas.entity.ChainEntity;

import java.util.Set;

/**
 * @author liubc
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
     * 持久化链路
     *
     * @param chainEntity
     */
    void chainPersistence(ChainEntity chainEntity);

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
     * 获取新的企业entId名单
     *
     * @return
     */
    Set<String> calNewEntIdList();
}
