package com.chinadaas.repository;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.entity.*;

import java.util.List;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 节点操作
 * @createTime 2021.07.20
 */
public interface NodeOperationRepository {

    /**
     * 识别企业点
     *
     * @param entId
     * @return
     */
    SourceEntity findSourceNode(String entId);

    /**
     * 识别决策权点（母公司 or 最终控股股东）
     *
     * @param entId
     * @param modelType
     * @return
     */
    List<DecisionEntity> findDecisionNode(String entId, ModelType modelType);

    /**
     * 识别单一大股东点
     *
     * @param entId
     * @return
     */
    SingleShareHolderEntity findSingleShareHolderNode(String entId);

    /**
     * 识别上市披露点
     *
     * @param entId
     * @return
     */
    ListDisclosureEntity findListDisclosureNode(String entId);

    /**
     * 节点修复
     *
     * @param entId
     */
    void nodeFix(String entId);

    /**
     * 节点查询
     *
     * @param entId
     * @return
     */
    NodeEntity nodeFind(String entId);

}
