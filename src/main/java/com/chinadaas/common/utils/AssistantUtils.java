package com.chinadaas.common.utils;


import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.model.ChainModel;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 辅助工具
 * @createTime 2021.07.01
 */
public abstract class AssistantUtils {

    private final static Map<String, String> ENT_STATUS_DESC = Maps.newHashMap();

    static {
        ENT_STATUS_DESC.put("1", "在营(开业)");
        ENT_STATUS_DESC.put("2", "吊销");
        ENT_STATUS_DESC.put("3", "注销");
        ENT_STATUS_DESC.put("4", "迁出");
        ENT_STATUS_DESC.put("5", "撤销");
        ENT_STATUS_DESC.put("6", "临时(个体工商户使用)");
        ENT_STATUS_DESC.put("8", "停业");
        ENT_STATUS_DESC.put("9", "其他");
        ENT_STATUS_DESC.put("21", "吊销未注销");
        ENT_STATUS_DESC.put("22", "吊销已注销");
    }

    /**
     * entStatus转换为entStatusDesc
     *
     * @param entStatus
     * @return
     */
    public static String getEntStatusDesc(String entStatus) {

        if (StringUtils.isBlank(entStatus)) {
            return "";
        }

        if (ENT_STATUS_DESC.containsKey(entStatus.trim())) {
            return ENT_STATUS_DESC.get(entStatus.trim());
        }

        return "";
    }

    /**
     * 投资类型转换节点类型
     *
     * @param invType
     * @return
     */
    public static String getNodeType(String invType) {
        /**
         * 701：境外企业
         * 702：组织机构
         * 703：组合计划
         * 704：工商企业
         * 20：自然人
         * 35：境外自然人
         * 90：其他
         */

        String type = "3";
        if ("701".equals(invType)
                || "702".equals(invType)
                || "703".equals(invType)
                || "704".equals(invType)) {

            type = "1";
        } else if (StringUtils.isBlank(invType)
                || "20".equals(invType)
                || "35".equals(invType)) {

            type = "2";
        }
        return type;
    }

    public static ChainEntity modelTransferToEntity(ChainModel chainModel) {

        ModelStatus resultStatus = chainModel.getResultStatus();
        NodeWrapper sourceNode = chainModel.getSourceNode();
        NodeWrapper targetNode = chainModel.getTargetNode();

        ChainEntity chainEntity = new ChainEntity();
        chainEntity.setSourceEntId(sourceNode.getEntId());
        chainEntity.setSourceName(sourceNode.getEntName());

        if (ModelStatus.COMPLETE_RESULT.equals(resultStatus)) {
            chainEntity.setTargetEntId(targetNode.getEntId());
            chainEntity.setTargetName(targetNode.getEntName());
            chainEntity.setGroupEntId(targetNode.getEntId());
            chainEntity.setGroupName(targetNode.getEntName());
            return chainEntity;
        }

        final String UNKNOWN_ID = "-1";
        final String UNKNOWN_NAME = "";
        chainEntity.setTargetEntId(UNKNOWN_ID);
        chainEntity.setTargetName(UNKNOWN_NAME);
        chainEntity.setGroupEntId(UNKNOWN_ID);
        chainEntity.setGroupName(UNKNOWN_NAME);
        return chainEntity;
    }

}
