package com.chinadaas.repository;

import com.chinadaas.common.constant.ModelType;
import com.chinadaas.component.wrapper.LinkWrapper;
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
     * 节点查询
     *
     * @param entId
     * @return
     */
    NodeEntity nodeFind(String entId);

    /**
     * 获取两点间信息
     *
     * @param sourceId
     * @param targetId
     * @param targetToSourceLayer
     * @return
     */
    List<TwoNodesEntity> sourceToTargetUseInv(String sourceId, String targetId, long targetToSourceLayer);

    /**
     * 获取两点间信息
     *
     * @param sourceId
     * @param personId
     * @param personToSourceLayer
     * @return
     */
    List<TwoNodesEntity> sourceToPersonUseInv(String sourceId, String personId, long personToSourceLayer);

    /**
     * 获取两点间信息（上市披露）
     *
     * @param sourceId
     * @param targetId
     * @return
     */
    List<TwoNodesEntity> sourceToTargetUseGroupParent(String sourceId, String targetId);

    /**
     * 获取可以替换groupparent边的teninvmerge边
     *
     * @param fromId
     * @param toId
     * @return
     */
    LinkWrapper groupParentMappingTenInvMerge(long fromId, long toId);

}
