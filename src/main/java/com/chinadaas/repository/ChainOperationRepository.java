package com.chinadaas.repository;

import com.chinadaas.common.constant.ModelType;
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
     * @param modelType
     * @return
     */
    ChainEntity chainQuery(String entId, ModelType modelType);

    /**
     * 链路批量查询
     *
     * @param entIds
     * @return
     */
    List<ChainEntity> chainBatchQuery(Set<String> entIds);

    /**
     * 链路修复
     * @param parentId
     * @param parentName
     * @param parentType
     * @param totalChainLength
     * @param nodeEntId
     * @param modelType
     */
    void chainFix(String parentId,
                  String parentName,
                  String parentType,
                  long totalChainLength,
                  String nodeEntId,
                  ModelType modelType);

    /**
     * 持久化链路（母公司 or 最终控股股东）
     *
     * @param chainEntity
     * @param modelType
     */
    void chainPersistence(ChainEntity chainEntity, ModelType modelType);

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
     * @param modelType
     * @return
     */
    Set<String> treeQuery(String entId, ModelType modelType);

    /**
     * 获取需要修复的entId名单
     *
     * @return
     */
    Set<String> obtainParentFixEntIds();

    /**
     * 获取用于查询最终控股股东的候选entId
     *
     * @return
     */
    Set<String> obtainFinCtrlEntIds();

    /**
     * 获取需要修复的entId名单
     *
     * @return
     */
    Set<String> obtainFinCtrlFixEntIds();
}
