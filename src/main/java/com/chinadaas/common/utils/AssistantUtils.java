package com.chinadaas.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.TargetType;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.commons.type.RelationType;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.entity.SuperCorporationEntity;
import com.chinadaas.model.ChainModel;
import com.chinadaas.model.SuperCorporationModel;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 辅助工具
 * @createTime 2021.07.01
 */
public abstract class AssistantUtils {

    /**
     * 投资
     */
    public static final int INV = 1;

    /**
     * 任职
     */
    public static final int STAFF = 2;

    /**
     * 同一电话
     */
    public static final int ENT_TEL = 3;

    /**
     * 同一公司地址
     */
    public static final int ENT_ADDR = 4;

    /**
     * 同一住宅地址
     */
    public static final int PER_ADDR = 5;

    /**
     * 控股关系
     */
    public static final int HOLD = 6;

    /**
     * 参股关系
     */
    public static final int JOIN = 7;

    /**
     * 控股
     */
    public static final int TEN_HOLD = 11;

    /**
     * 上市公司实控
     */
    public static final int CONTROL = 12;

    /**
     * 上市披露
     */
    public static final int GROUP_PARENT = 13;

    /**
     * 默认值
     */
    public static final int DEFAULT = 0;

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
        /*
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

    public static int parseRelationType(String typeName) {

        switch (typeName) {
            case "inv":
            case "teninvmerge":
                return RelationType.INV;
            case "staff":
            case "legal":
                return STAFF;
            case "enttel":
                return ENT_TEL;
            case "entaddr":
                return ENT_ADDR;
            case "peraddr":
                return PER_ADDR;
            case "hold":
                return HOLD;
            case "join":
                return JOIN;
            case "tenhold":
            case "tenholdmerge":
                return TEN_HOLD;
            case "control":
                return CONTROL;
            case "groupparent":
                return GROUP_PARENT;
            default:
                return DEFAULT;
        }

    }

    public static ChainEntity modelTransferToEntityOfChain(ChainModel chainModel) {

        ModelStatus resultStatus = chainModel.getResultStatus();
        NodeWrapper sourceNode = chainModel.getSourceNode();
        NodeWrapper targetNode = chainModel.getTargetNode();
        TargetType targetType = chainModel.getTargetType();
        long chainLength = chainModel.getChainLength();

        ChainEntity chainEntity = new ChainEntity();
        chainEntity.setSourceEntId(sourceNode.getEntId());
        chainEntity.setSourceName(sourceNode.getEntName());
        chainEntity.setTargetType(targetType.toString());
        chainEntity.setTemp2SourceLayer(chainLength);
        chainEntity.setTarget2SourceLayer(chainLength);

        if (ModelStatus.COMPLETE_RESULT.equals(resultStatus)) {
            // zs: 节点类型为自然人，特殊处理
            int type = targetNode.getType();

            if (NodeType.PERSON == type) {
                chainEntity.setTempEntId(targetNode.getZsId());
                chainEntity.setTargetEntId(targetNode.getZsId());
            } else {
                chainEntity.setTempEntId(targetNode.getEntId());
                chainEntity.setTargetEntId(targetNode.getEntId());
            }

            chainEntity.setTempName(targetNode.getEntName());
            chainEntity.setTargetName(targetNode.getEntName());
            return chainEntity;
        }


        chainEntity.setTempEntId(ChainConst.UNKNOWN_ID);
        chainEntity.setTempName(ChainConst.UNKNOWN_NAME);
        chainEntity.setTargetEntId(ChainConst.UNKNOWN_ID);
        chainEntity.setTargetName(ChainConst.UNKNOWN_NAME);
        return chainEntity;
    }

    public static SuperCorporationEntity modelTransferToEntityOfSC(SuperCorporationModel superCorporationModel) {

        SuperCorporationEntity superCorporationEntity = new SuperCorporationEntity();

        superCorporationEntity.setEntId(superCorporationModel.getEntId());
        superCorporationEntity.setEntName(superCorporationModel.getEntName());
        superCorporationEntity.setSourceProperty(superCorporationModel.getSourceProperty());
        superCorporationEntity.setFinCtrlId(superCorporationModel.getFinCtrlId());
        superCorporationEntity.setFinCtrlName(superCorporationModel.getFinCtrlName());
        superCorporationEntity.setFinCtrlProperty(superCorporationModel.getFinCtrlProperty());
        superCorporationEntity.setParent2SourceRelation(superCorporationModel.getParent2SourceRelation());
        superCorporationEntity.setParentId(superCorporationModel.getParentId());
        superCorporationEntity.setParentName(superCorporationModel.getParentName());
        superCorporationEntity.setParentRegno(superCorporationModel.getParentRegno());
        superCorporationEntity.setParentCreditcode(superCorporationModel.getParentCreditcode());
        superCorporationEntity.setCtrl2ParentPath(superCorporationModel.getCtrl2ParentPath());
        superCorporationEntity.setParent2SourcePath(superCorporationModel.getParent2SourcePath());
        superCorporationEntity.setCtrl2SourcePath(superCorporationModel.getCtrl2SourcePath());
        superCorporationEntity.setParentProperty(superCorporationModel.getParentProperty());
        superCorporationEntity.setParent2SourceCgzb(superCorporationModel.getParent2SourceCgzb());
        superCorporationEntity.setCtrl2SourceCgzb(superCorporationModel.getCtrl2SourceCgzb());
        superCorporationEntity.setCtrl2ParentCgzb(superCorporationModel.getCtrl2ParentCgzb());
        superCorporationEntity.setEmId(superCorporationModel.getEmId());

        return superCorporationEntity;
    }

    public static String superCorporationRecord(SuperCorporationEntity superCorporationEntity) {

        List<String> recordItems = Arrays.asList(
                StringUtils.trimToEmpty(superCorporationEntity.getEntId()),
                StringUtils.trimToEmpty(superCorporationEntity.getEntName()),
                StringUtils.trimToEmpty("null".equals(JSONObject.toJSONString(superCorporationEntity.getSourceProperty()))
                        ? "" : JSONObject.toJSONString(superCorporationEntity.getSourceProperty())),
                StringUtils.trimToEmpty(superCorporationEntity.getFinCtrlId()),
                StringUtils.trimToEmpty(superCorporationEntity.getFinCtrlName()),
                StringUtils.trimToEmpty("null".equals(JSONObject.toJSONString(superCorporationEntity.getFinCtrlProperty()))
                        ? "" : JSONObject.toJSONString(superCorporationEntity.getFinCtrlProperty())),
                StringUtils.trimToEmpty(superCorporationEntity.getParent2SourceRelation()),
                StringUtils.trimToEmpty(superCorporationEntity.getParentId()),
                StringUtils.trimToEmpty(superCorporationEntity.getParentName()),
                StringUtils.trimToEmpty(superCorporationEntity.getParentRegno()),
                StringUtils.trimToEmpty(superCorporationEntity.getParentCreditcode()),
                StringUtils.trimToEmpty("null".equals(JSONObject.toJSONString(Neo4jResultParseUtils.getFilterPath(superCorporationEntity.getCtrl2ParentPath())))
                        ? "" : JSONObject.toJSONString(Neo4jResultParseUtils.getFilterPath(superCorporationEntity.getCtrl2ParentPath()))),
                StringUtils.trimToEmpty("null".equals(JSONObject.toJSONString(Neo4jResultParseUtils.getFilterPath(superCorporationEntity.getParent2SourcePath())))
                        ? "" : JSONObject.toJSONString(Neo4jResultParseUtils.getFilterPath(superCorporationEntity.getParent2SourcePath()))),
                StringUtils.trimToEmpty(
                        "null".equals(JSONObject.toJSONString(Neo4jResultParseUtils.getFilterPath(superCorporationEntity.getCtrl2SourcePath())))
                                ? "" : JSONObject.toJSONString(Neo4jResultParseUtils.getFilterPath(superCorporationEntity.getCtrl2SourcePath()))
                ),
                StringUtils.trimToEmpty("null".equals(JSONObject.toJSONString(superCorporationEntity.getParentProperty()))
                        ? "" : JSONObject.toJSONString(superCorporationEntity.getParentProperty())),
                StringUtils.trimToEmpty(superCorporationEntity.getParent2SourceCgzb()),
                StringUtils.trimToEmpty(superCorporationEntity.getCtrl2SourceCgzb()),
                StringUtils.trimToEmpty(superCorporationEntity.getCtrl2ParentCgzb()),
                StringUtils.trimToEmpty(superCorporationEntity.getEmId())

        );

        return String.join("\u0001", recordItems);

    }

}
