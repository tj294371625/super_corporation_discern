package com.chinadaas.service;

import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.entity.TwoNodesEntity;

import java.util.List;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 节点操作
 * @createTime 2021.07.23
 */
public interface NodeOperationService {

    /**
     * 企业是否在营，true表示在营，false表示非在营
     *
     * @param entId
     * @return
     */
    boolean managementStatus(String entId);

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
     * 获取可以替换groupparent边的teninvmerge边
     *
     * @param fromId
     * @param toId
     * @return
     */
    LinkWrapper groupParentMappingTenInvMerge(long fromId, long toId);
}
