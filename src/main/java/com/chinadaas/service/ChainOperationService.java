package com.chinadaas.service;

import com.chinadaas.common.constant.ModelType;
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
     * @param finCtrl
     */
    void recursiveChainFix(String entId, ModelType finCtrl);

    /**
     * 持久化链路（母公司 or 最终控股股东）
     *
     * @param chainEntity
     * @param modelType
     */
    void chainPersistence(ChainEntity chainEntity, ModelType modelType);

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
     * @param parent
     * @return
     */
    ChainEntity chainQuery(String entId, ModelType parent);

    /**
     * 获取需要修复的entId名单
     *
     * @return
     */
    Set<String> parentFixEntIds();

    /**
     * 获取用于查询最终控股股东的候选entId
     *
     * @return
     */
    Set<String> queryFinCtrlEntIds();

    /**
     * 获取上市披露公司的前一个公司的企业标识
     *
     * @param sourceEntId
     * @param targetEntId
     * @return
     */
    String obtainEntBeforeDisclosure(String sourceEntId, String targetEntId);

    /**
     * 获取需要修复的entId名单
     *
     * @return
     */
    Set<String> finCtrlFixEntIds();
}
