package com.chinadaas.common.util;

import com.alibaba.fastjson.JSONObject;
import com.chinadaas.common.constant.ChainConst;
import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.TargetType;
import com.chinadaas.commons.graph.model.GraphDto;
import com.chinadaas.commons.graph.model.NodeDto;
import com.chinadaas.commons.graph.model.RelationDto;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.commons.type.RelationType;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.ChainEntity;
import com.chinadaas.entity.SuperCorporationEntity;
import com.chinadaas.entity.old.*;
import com.chinadaas.model.ChainModel;
import com.chinadaas.model.SuperCorporationModel;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
        chainEntity.setId(sourceNode.obtainEntId());
        chainEntity.setSourceEntId(sourceNode.obtainEntId());
        chainEntity.setSourceName(sourceNode.obtainEntName());
        chainEntity.setTargetType(targetType.toString());
        chainEntity.setTemp2SourceLayer(chainLength);
        chainEntity.setTarget2SourceLayer(chainLength);

        if (ModelStatus.COMPLETE_RESULT.equals(resultStatus)) {
            // zs: 节点类型为自然人，特殊处理
            int type = targetNode.getType();

            if (NodeType.PERSON == type) {
                chainEntity.setTempEntId(targetNode.obtainZsId());
                chainEntity.setTargetEntId(targetNode.obtainZsId());
            } else {
                chainEntity.setTempEntId(targetNode.obtainEntId());
                chainEntity.setTargetEntId(targetNode.obtainEntId());
            }

            chainEntity.setTempName(targetNode.obtainEntName());
            chainEntity.setTargetName(targetNode.obtainEntName());
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

        PathWrapper ctrl2ParentPath = superCorporationModel.getCtrl2ParentPath();
        if (Objects.nonNull(ctrl2ParentPath)) {
            PathWrapper filterPath = Neo4jResultParseUtils.getFilterPath(ctrl2ParentPath);
            superCorporationEntity.setCtrl2ParentPath(filterPath);
        } else {
            superCorporationEntity.setCtrl2ParentPath(null);
        }

        PathWrapper parent2SourcePath = superCorporationModel.getParent2SourcePath();
        if (Objects.nonNull(parent2SourcePath)) {
            PathWrapper filterPath = Neo4jResultParseUtils.getFilterPath(parent2SourcePath);
            superCorporationEntity.setParent2SourcePath(filterPath);
        } else {
            superCorporationEntity.setParent2SourcePath(null);
        }

        PathWrapper ctrl2SourcePath = superCorporationModel.getCtrl2SourcePath();
        if (Objects.nonNull(ctrl2SourcePath)) {
            PathWrapper filterPath = Neo4jResultParseUtils.getFilterPath(ctrl2SourcePath);
            superCorporationEntity.setCtrl2SourcePath(filterPath);
        } else {
            superCorporationEntity.setCtrl2SourcePath(null);
        }


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
                StringUtils.trimToEmpty("null".equals(JSONObject.toJSONString(superCorporationEntity.getCtrl2ParentPath()))
                        ? "" : JSONObject.toJSONString(superCorporationEntity.getCtrl2ParentPath())),
                StringUtils.trimToEmpty("null".equals(JSONObject.toJSONString(superCorporationEntity.getParent2SourcePath()))
                        ? "" : JSONObject.toJSONString(superCorporationEntity.getParent2SourcePath())),
                StringUtils.trimToEmpty(
                        "null".equals(JSONObject.toJSONString(superCorporationEntity.getCtrl2SourcePath()))
                                ? "" : JSONObject.toJSONString(superCorporationEntity.getCtrl2SourcePath())
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

    public static List<String> memberRecord(List<BaseEntInfo> baseEntInfos) {
        List<String> recordList = baseEntInfos.stream()
                .map(
                        baseEntInfo -> {
                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(baseEntInfo.getParent_id()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEntid()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEntname()),
                                    StringUtils.trimToEmpty(baseEntInfo.getInvtype()),
                                    StringUtils.trimToEmpty(baseEntInfo.getRegno()),
                                    StringUtils.trimToEmpty(baseEntInfo.getCreditcode()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEsdate()),
                                    StringUtils.trimToEmpty(baseEntInfo.getIndustryphy()),
                                    StringUtils.trimToEmpty(baseEntInfo.getRegcap()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEntstatus()),
                                    StringUtils.trimToEmpty(baseEntInfo.getRegcapcur()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEnttype()),
                                    StringUtils.trimToEmpty(baseEntInfo.getIslist()),
                                    StringUtils.trimToEmpty(baseEntInfo.getCode()),
                                    StringUtils.trimToEmpty(baseEntInfo.getStockcode()),
                                    StringUtils.trimToEmpty(baseEntInfo.getBriefname()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEnt_country()),
                                    StringUtils.trimToEmpty(baseEntInfo.getType()),
                                    StringUtils.trimToEmpty(baseEntInfo.getIndustryco()),
                                    StringUtils.trimToEmpty(baseEntInfo.getProvince()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEnttype_desc()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(baseEntInfo.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(baseEntInfo.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(baseEntInfo.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(baseEntInfo.getProvince_desc()),
                                    StringUtils.trimToEmpty(baseEntInfo.getBreak_law_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getPunish_break_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getPunished_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getAbnormity_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getMab_info_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getFinalcase_count()),
                                    StringUtils.trimToEmpty(baseEntInfo.getRelation()),
                                    StringUtils.trimToEmpty(baseEntInfo.getRelation_density()),
                                    StringUtils.trimToEmpty(baseEntInfo.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(baseEntInfo.getPath()))
                                                    ? "" : JSONObject.toJSONString(baseEntInfo.getPath())
                                    ),
                                    StringUtils.trimToEmpty(baseEntInfo.getFinal_cgzb())

                            );
                            return String.join("\u0001", recordItems);
                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static List<String> discernAndMajorPersonRecord(List<ParentAndMajorInvPersonInfo> parentAndMajorInvPersonInfos) {
        List<String> recordList = parentAndMajorInvPersonInfos.stream()
                .map(
                        parentAndMajorInvPersonInfo -> {
                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getParent_id()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getName()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getZsid()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getZspid()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getPalgorithmid()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getPerson_country()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getPerson_country_desc()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getConprop_person2parent()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getConprop_person2sub()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getConprop_parent2sub()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEntid()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEntname()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getInvtype()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getRegno()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getCreditcode()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEsdate()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getIndustryphy()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getRegcap()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEntstatus()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getRegcapcur()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEnttype()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getIslist()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getCode()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getStockcode()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getBriefname()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEnt_country()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getType()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getIndustryco()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getProvince()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEnttype_desc()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getProvince_desc()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getBreak_law_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getPunish_break_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getPunished_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getAbnormity_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getMab_info_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getFinalcase_count()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getRelation()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getRelation_density()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(parentAndMajorInvPersonInfo.getPath()))
                                                    ? "" : JSONObject.toJSONString(parentAndMajorInvPersonInfo.getPath())
                                    ),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getHolderrto_person2parent()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getHolderrto_person2sub()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getHolderrto_parent2sub()),
                                    StringUtils.trimToEmpty(parentAndMajorInvPersonInfo.getEmid())

                            );
                            return String.join("\u0001", recordItems);
                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static List<String> discernAndStaffRecord(List<StaffAndParentCommonInfo> staffAndParentCommonInfos) {
        List<String> recordList = staffAndParentCommonInfos.stream()
                .map(
                        staffAndParent -> {
                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(staffAndParent.getParent_id()),
                                    StringUtils.trimToEmpty(staffAndParent.getName()),
                                    StringUtils.trimToEmpty(staffAndParent.getZsid()),
                                    StringUtils.trimToEmpty(staffAndParent.getZspid()),
                                    StringUtils.trimToEmpty(staffAndParent.getPalgorithmid()),
                                    StringUtils.trimToEmpty(staffAndParent.getPerson_country()),
                                    StringUtils.trimToEmpty(staffAndParent.getPerson_country_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getPosition()),
                                    StringUtils.trimToEmpty(staffAndParent.getPosition_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getConprop_person2sub()),
                                    StringUtils.trimToEmpty(staffAndParent.getConprop_parent2sub()),
                                    StringUtils.trimToEmpty(staffAndParent.getEntid()),
                                    StringUtils.trimToEmpty(staffAndParent.getEntname()),
                                    StringUtils.trimToEmpty(staffAndParent.getInvtype()),
                                    StringUtils.trimToEmpty(staffAndParent.getRegno()),
                                    StringUtils.trimToEmpty(staffAndParent.getCreditcode()),
                                    StringUtils.trimToEmpty(staffAndParent.getEsdate()),
                                    StringUtils.trimToEmpty(staffAndParent.getIndustryphy()),
                                    StringUtils.trimToEmpty(staffAndParent.getRegcap()),
                                    StringUtils.trimToEmpty(staffAndParent.getEntstatus()),
                                    StringUtils.trimToEmpty(staffAndParent.getRegcapcur()),
                                    StringUtils.trimToEmpty(staffAndParent.getEnttype()),
                                    StringUtils.trimToEmpty(staffAndParent.getIslist()),
                                    StringUtils.trimToEmpty(staffAndParent.getCode()),
                                    StringUtils.trimToEmpty(staffAndParent.getStockcode()),
                                    StringUtils.trimToEmpty(staffAndParent.getBriefname()),
                                    StringUtils.trimToEmpty(staffAndParent.getEnt_country()),
                                    StringUtils.trimToEmpty(staffAndParent.getType()),
                                    StringUtils.trimToEmpty(staffAndParent.getIndustryco()),
                                    StringUtils.trimToEmpty(staffAndParent.getProvince()),
                                    StringUtils.trimToEmpty(staffAndParent.getEnttype_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getProvince_desc()),
                                    StringUtils.trimToEmpty(staffAndParent.getBreak_law_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getPunish_break_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getPunished_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getAbnormity_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getMab_info_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getFinalcase_count()),
                                    StringUtils.trimToEmpty(staffAndParent.getRelation()),
                                    StringUtils.trimToEmpty(staffAndParent.getRelation_density()),
                                    StringUtils.trimToEmpty(staffAndParent.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(staffAndParent.getPath()))
                                                    ? "" : JSONObject.toJSONString(staffAndParent.getPath())

                                    ),
                                    StringUtils.trimToEmpty(staffAndParent.getHolderrto_person2sub()),
                                    StringUtils.trimToEmpty(staffAndParent.getHolderrto_parent2sub()),
                                    StringUtils.trimToEmpty(staffAndParent.getEmid())

                            );
                            return String.join("\u0001", recordItems);
                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static List<String> discernLegalOutRecord(List<StaffAndParentCommonInfo> staffAndParentCommonInfos) {
        List<String> recordList = staffAndParentCommonInfos.stream()
                .map(
                        basePersonInfo -> {

                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(basePersonInfo.getParent_id()),
                                    StringUtils.trimToEmpty(basePersonInfo.getName()),
                                    StringUtils.trimToEmpty(basePersonInfo.getZsid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getZspid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPalgorithmid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPerson_country()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPerson_country_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntname()),
                                    StringUtils.trimToEmpty(basePersonInfo.getInvtype()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegno()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCreditcode()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEsdate()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryphy()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegcap()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntstatus()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegcapcur()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnttype()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIslist()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCode()),
                                    StringUtils.trimToEmpty(basePersonInfo.getStockcode()),
                                    StringUtils.trimToEmpty(basePersonInfo.getBriefname()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnt_country()),
                                    StringUtils.trimToEmpty(basePersonInfo.getType()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryco()),
                                    StringUtils.trimToEmpty(basePersonInfo.getProvince()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnttype_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getProvince_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getBreak_law_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPunish_break_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPunished_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getAbnormity_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getMab_info_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getFinalcase_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRelation()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRelation_density()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(basePersonInfo.getPath())) ? "" : JSONObject.toJSONString(basePersonInfo.getPath())

                                    ),
                                    StringUtils.trimToEmpty(basePersonInfo.getFinal_cgzb()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEmid())
                            );
                            return String.join("\u0001", recordItems);
                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static List<String> controlPersonLegalRecord(List<BasePersonInfo> basePersonInfos) {
        List<String> recordList = basePersonInfos.stream()
                .map(
                        basePersonInfo -> {

                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(basePersonInfo.getParent_id()),
                                    StringUtils.trimToEmpty(basePersonInfo.getName()),
                                    StringUtils.trimToEmpty(basePersonInfo.getZsid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getZspid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPalgorithmid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPerson_country()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPerson_country_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntid()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntname()),
                                    StringUtils.trimToEmpty(basePersonInfo.getInvtype()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegno()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCreditcode()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEsdate()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryphy()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegcap()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntstatus()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegcapcur()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnttype()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIslist()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCode()),
                                    StringUtils.trimToEmpty(basePersonInfo.getStockcode()),
                                    StringUtils.trimToEmpty(basePersonInfo.getBriefname()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnt_country()),
                                    StringUtils.trimToEmpty(basePersonInfo.getType()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryco()),
                                    StringUtils.trimToEmpty(basePersonInfo.getProvince()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnttype_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getProvince_desc()),
                                    StringUtils.trimToEmpty(basePersonInfo.getBreak_law_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPunish_break_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getPunished_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getAbnormity_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getMab_info_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getFinalcase_count()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRelation()),
                                    StringUtils.trimToEmpty(basePersonInfo.getRelation_density()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(basePersonInfo.getPath())) ? "" : JSONObject.toJSONString(basePersonInfo.getPath())

                                    ),
                                    StringUtils.trimToEmpty(basePersonInfo.getFinal_cgzb()),
                                    StringUtils.trimToEmpty(basePersonInfo.getEmid())
                            );
                            return String.join("\u0001", recordItems);


                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static List<String> personOutControlRecord(List<PersonOutControlInfo> personOutControlInfos) {

        List<String> recordList = personOutControlInfos.stream()
                .map(
                        personOutControl -> {

                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(personOutControl.getParent_id()),
                                    StringUtils.trimToEmpty(personOutControl.getName()),
                                    StringUtils.trimToEmpty(personOutControl.getZsid()),
                                    StringUtils.trimToEmpty(personOutControl.getZspid()),
                                    StringUtils.trimToEmpty(personOutControl.getPalgorithmid()),
                                    StringUtils.trimToEmpty(personOutControl.getPerson_country()),
                                    StringUtils.trimToEmpty(personOutControl.getPerson_country_desc()),
                                    StringUtils.trimToEmpty(personOutControl.getEntid()),
                                    StringUtils.trimToEmpty(personOutControl.getEntname()),
                                    StringUtils.trimToEmpty(personOutControl.getInvtype()),
                                    StringUtils.trimToEmpty(personOutControl.getRegno()),
                                    StringUtils.trimToEmpty(personOutControl.getCreditcode()),
                                    StringUtils.trimToEmpty(personOutControl.getEsdate()),
                                    StringUtils.trimToEmpty(personOutControl.getIndustryphy()),
                                    StringUtils.trimToEmpty(personOutControl.getRegcap()),
                                    StringUtils.trimToEmpty(personOutControl.getEntstatus()),
                                    StringUtils.trimToEmpty(personOutControl.getRegcapcur()),
                                    StringUtils.trimToEmpty(personOutControl.getEnttype()),
                                    StringUtils.trimToEmpty(personOutControl.getIslist()),
                                    StringUtils.trimToEmpty(personOutControl.getCode()),
                                    StringUtils.trimToEmpty(personOutControl.getStockcode()),
                                    StringUtils.trimToEmpty(personOutControl.getBriefname()),
                                    StringUtils.trimToEmpty(personOutControl.getEnt_country()),
                                    StringUtils.trimToEmpty(personOutControl.getType()),
                                    StringUtils.trimToEmpty(personOutControl.getIndustryco()),
                                    StringUtils.trimToEmpty(personOutControl.getProvince()),
                                    StringUtils.trimToEmpty(personOutControl.getEnttype_desc()),
                                    StringUtils.trimToEmpty(personOutControl.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(personOutControl.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(personOutControl.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(personOutControl.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(personOutControl.getProvince_desc()),
                                    StringUtils.trimToEmpty(personOutControl.getBreak_law_count()),
                                    StringUtils.trimToEmpty(personOutControl.getPunish_break_count()),
                                    StringUtils.trimToEmpty(personOutControl.getPunished_count()),
                                    StringUtils.trimToEmpty(personOutControl.getAbnormity_count()),
                                    StringUtils.trimToEmpty(personOutControl.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(personOutControl.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(personOutControl.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(personOutControl.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(personOutControl.getMab_info_count()),
                                    StringUtils.trimToEmpty(personOutControl.getFinalcase_count()),
                                    StringUtils.trimToEmpty(personOutControl.getRelation()),
                                    StringUtils.trimToEmpty(personOutControl.getRelation_density()),
                                    StringUtils.trimToEmpty(personOutControl.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(personOutControl.getCtrl2parent_path())) ? "" : JSONObject.toJSONString(personOutControl.getCtrl2parent_path())

                                    ),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(personOutControl.getCtrl2source_path())) ? "" : JSONObject.toJSONString(personOutControl.getCtrl2source_path())

                                    ),
                                    StringUtils.trimToEmpty(personOutControl.getCtrl2parent_cgzb()),
                                    StringUtils.trimToEmpty(personOutControl.getCtrl2source_cgzb()),
                                    StringUtils.trimToEmpty(personOutControl.getEmid())
                            );
                            return String.join("\u0001", recordItems);


                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static List<String> majorPersonRecord(List<MajorInvPersonInfo> majorInvPersonInfos) {

        List<String> recordList = majorInvPersonInfos.stream()
                .map(
                        majorInvPerson -> {
                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(majorInvPerson.getParent_id()),
                                    StringUtils.trimToEmpty(majorInvPerson.getName()),
                                    StringUtils.trimToEmpty(majorInvPerson.getZsid()),
                                    StringUtils.trimToEmpty(majorInvPerson.getZspid()),
                                    StringUtils.trimToEmpty(majorInvPerson.getPalgorithmid()),
                                    StringUtils.trimToEmpty(majorInvPerson.getPerson_country()),
                                    StringUtils.trimToEmpty(majorInvPerson.getPerson_country_desc()),
                                    StringUtils.trimToEmpty(majorInvPerson.getConprop_person2parent()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEntid()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEntname()),
                                    StringUtils.trimToEmpty(majorInvPerson.getInvtype()),
                                    StringUtils.trimToEmpty(majorInvPerson.getRegno()),
                                    StringUtils.trimToEmpty(majorInvPerson.getCreditcode()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEsdate()),
                                    StringUtils.trimToEmpty(majorInvPerson.getIndustryphy()),
                                    StringUtils.trimToEmpty(majorInvPerson.getRegcap()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEntstatus()),
                                    StringUtils.trimToEmpty(majorInvPerson.getRegcapcur()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEnttype()),
                                    StringUtils.trimToEmpty(majorInvPerson.getIslist()),
                                    StringUtils.trimToEmpty(majorInvPerson.getCode()),
                                    StringUtils.trimToEmpty(majorInvPerson.getStockcode()),
                                    StringUtils.trimToEmpty(majorInvPerson.getBriefname()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEnt_country()),
                                    StringUtils.trimToEmpty(majorInvPerson.getType()),
                                    StringUtils.trimToEmpty(majorInvPerson.getIndustryco()),
                                    StringUtils.trimToEmpty(majorInvPerson.getProvince()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEnttype_desc()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(majorInvPerson.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(majorInvPerson.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(majorInvPerson.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(majorInvPerson.getProvince_desc()),
                                    StringUtils.trimToEmpty(majorInvPerson.getBreak_law_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getPunish_break_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getPunished_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getAbnormity_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getMab_info_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getFinalcase_count()),
                                    StringUtils.trimToEmpty(majorInvPerson.getRelation()),
                                    StringUtils.trimToEmpty(majorInvPerson.getRelation_density()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(majorInvPerson.getPath())) ? "" : JSONObject.toJSONString(majorInvPerson.getPath())

                                    ),
                                    StringUtils.trimToEmpty(majorInvPerson.getHolderrto_person2parent()),
                                    StringUtils.trimToEmpty(majorInvPerson.getFinal_cgzb()),
                                    StringUtils.trimToEmpty(majorInvPerson.getEmid())

                            );
                            return String.join("\u0001", recordItems);


                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static List<String> staffRecord(List<StaffPerson> staffPeople) {

        List<String> recordList = staffPeople.stream()
                .map(
                        staffPerson -> {

                            List<String> recordItems = Arrays.asList(
                                    StringUtils.trimToEmpty(staffPerson.getParent_id()),
                                    StringUtils.trimToEmpty(staffPerson.getName()),
                                    StringUtils.trimToEmpty(staffPerson.getZsid()),
                                    StringUtils.trimToEmpty(staffPerson.getZspid()),
                                    StringUtils.trimToEmpty(staffPerson.getPalgorithmid()),
                                    StringUtils.trimToEmpty(staffPerson.getPerson_country()),
                                    StringUtils.trimToEmpty(staffPerson.getPerson_country_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getPosition()),
                                    StringUtils.trimToEmpty(staffPerson.getPosition_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getEntid()),
                                    StringUtils.trimToEmpty(staffPerson.getEntname()),
                                    StringUtils.trimToEmpty(staffPerson.getInvtype()),
                                    StringUtils.trimToEmpty(staffPerson.getRegno()),
                                    StringUtils.trimToEmpty(staffPerson.getCreditcode()),
                                    StringUtils.trimToEmpty(staffPerson.getEsdate()),
                                    StringUtils.trimToEmpty(staffPerson.getIndustryphy()),
                                    StringUtils.trimToEmpty(staffPerson.getRegcap()),
                                    StringUtils.trimToEmpty(staffPerson.getEntstatus()),
                                    StringUtils.trimToEmpty(staffPerson.getRegcapcur()),
                                    StringUtils.trimToEmpty(staffPerson.getEnttype()),
                                    StringUtils.trimToEmpty(staffPerson.getIslist()),
                                    StringUtils.trimToEmpty(staffPerson.getCode()),
                                    StringUtils.trimToEmpty(staffPerson.getStockcode()),
                                    StringUtils.trimToEmpty(staffPerson.getBriefname()),
                                    StringUtils.trimToEmpty(staffPerson.getEnt_country()),
                                    StringUtils.trimToEmpty(staffPerson.getType()),
                                    StringUtils.trimToEmpty(staffPerson.getIndustryco()),
                                    StringUtils.trimToEmpty(staffPerson.getProvince()),
                                    StringUtils.trimToEmpty(staffPerson.getEnttype_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getEnt_country_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getRegcapcur_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getIndustryphy_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getIndustryco_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getProvince_desc()),
                                    StringUtils.trimToEmpty(staffPerson.getBreak_law_count()),
                                    StringUtils.trimToEmpty(staffPerson.getPunish_break_count()),
                                    StringUtils.trimToEmpty(staffPerson.getPunished_count()),
                                    StringUtils.trimToEmpty(staffPerson.getAbnormity_count()),
                                    StringUtils.trimToEmpty(staffPerson.getCaseinfo_count()),
                                    StringUtils.trimToEmpty(staffPerson.getCourtannoucement_count()),
                                    StringUtils.trimToEmpty(staffPerson.getGaccpenalty_count()),
                                    StringUtils.trimToEmpty(staffPerson.getJudicial_aid_count()),
                                    StringUtils.trimToEmpty(staffPerson.getMab_info_count()),
                                    StringUtils.trimToEmpty(staffPerson.getFinalcase_count()),
                                    StringUtils.trimToEmpty(staffPerson.getRelation()),
                                    StringUtils.trimToEmpty(staffPerson.getRelation_density()),
                                    StringUtils.trimToEmpty(staffPerson.getEntstatus_desc()),
                                    StringUtils.trimToEmpty(
                                            "null".equals(JSONObject.toJSONString(staffPerson.getPath())) ? "" : JSONObject.toJSONString(staffPerson.getPath())

                                    ),
                                    StringUtils.trimToEmpty(staffPerson.getFinal_cgzb()),
                                    StringUtils.trimToEmpty(staffPerson.getEmid())
                            );
                            return String.join("\u0001", recordItems);


                        }
                )
                .collect(Collectors.toList());

        return recordList;
    }

    public static void filterFinCtrlNodeProperties(Map<String, Object> nodeProperties, int nodeType) {

        if (NodeType.PERSON == nodeType) {
            nodeProperties.remove("nodeid");
            nodeProperties.put("invtype", "20");

            if (nodeProperties.containsKey("encode_v1")) {
                nodeProperties.put("palgorithmid", nodeProperties.remove("encode_v1"));
            }

        } else {
            filterCommonPartProperties(nodeProperties);
        }
    }

    public static void filterCommonPartProperties(Map<String, Object> nodeProperties) {
        nodeProperties.remove("zsid");

        if (nodeProperties.containsKey("nodeid")) {
            nodeProperties.put("entid", nodeProperties.remove("nodeid"));
        }

        if (nodeProperties.containsKey("entstatus")) {
            String entStatus = (String) nodeProperties.get("entstatus");
            String entStatusDesc = getEntStatusDesc(entStatus);
            nodeProperties.put("entstatus_desc", entStatusDesc);
        }
    }

    public static String getConprop(LinkWrapper link) {
        Map<String, Object> properties = link.getProperties();
        String conprop = (String) properties.get("conprop");
        if (StringUtils.isNotBlank(conprop)) {
            return conprop;
        }
        return "";

    }

    public static String getHolderrto(LinkWrapper link) {
        Map<String, Object> properties = link.getProperties();
        String holderrto = (String) properties.get("holderrto");
        if (StringUtils.isNotBlank(holderrto)) {
            return holderrto;
        }
        return "";

    }

    public static void generateZspId(Map<String, Object> personProperties, String parentId) {
        String zsId = (String) personProperties.get("zsid");

        if (StringUtils.isNotBlank(zsId)) {
            int index = zsId.indexOf("-");
            String zspId = zsId.substring(0, index);

            if (zspId.length() > 30) {
                if (parentId.equals(zspId)) {
                    personProperties.put(MemberConst.ZSPID, zspId);
                }
            }
        }

    }




    /*
     * 旧代码迁移
     * */

    public static <T> List<T> mapListToList(List<Map<String, Object>> source, Class<T> entityClass) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.emptyList();
        }
        return source.stream().map(m -> mapToObject(m, entityClass)).collect(Collectors.toList());
    }

    /**
     * 将map映射成对象
     *
     * @param source
     * @param entityClass
     * @param <T>
     * @return
     */
    public static <T> T mapToObject(Map source, Class<T> entityClass) {

        try {

            Map<String, Field> nameFieldMap = new HashMap<>();
            T t = entityClass.getDeclaredConstructor().newInstance();

            if (CollectionUtils.isEmpty(source)) {
                return t;
            }
            Class currentClass = entityClass;
            Field[] fields = entityClass.getDeclaredFields();

            while (true) {

                for (Field field : fields) {
                    String fieldName = getKeyName(field.getName());
                    nameFieldMap.put(fieldName, field);
                    org.springframework.data.mongodb.core.mapping.Field[] annotationsByType =
                            field.getAnnotationsByType(org.springframework.data.mongodb.core.mapping.Field.class);

                    for (org.springframework.data.mongodb.core.mapping.Field mapProperty : annotationsByType) {
                        String name1 = getKeyName(mapProperty.value());
                        if (name1.equals(fieldName)) {
                            continue;
                        }
                        nameFieldMap.put(name1, field);
                        nameFieldMap.remove(fieldName);
                    }
                }

                currentClass = currentClass.getSuperclass();
                if (currentClass == null) {
                    break;
                }
                fields = currentClass.getDeclaredFields();
            }

            source.forEach((key, value) -> {
                if (key instanceof String) {

                    String fieldName = getKeyName((String) key);

                    Field field = nameFieldMap.get(fieldName);

                    if (field == null) {
                        return;
                    }

                    if (value == null) {
                        return;
                    }
                    if (formatClass(field.getType()).equals(formatClass(value.getClass()))
                            ||
                            isSupperclass(field.getType(), value.getClass())
                    ) {
                        try {
                            field.setAccessible(true);
                            field.set(t, value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (value instanceof Map) {
                        try {
                            Object o = mapToObject((Map) value, field.getType());
                            field.setAccessible(true);
                            field.set(t, o);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (entityClass.equals(GraphDto.class)) {
                                if ("nodes".equals(field.getName())) {
                                    Set<NodeDto> nodes = new HashSet<>();
                                    List list = (List) value;
                                    for (Object o : list) {
                                        NodeDto nodeDto = mapToObject((Map) o, NodeDto.class);
                                        nodes.add(nodeDto);
                                    }
                                    field.setAccessible(true);
                                    field.set(t, nodes);
                                }

                                if ("links".equals(field.getName())) {
                                    Set<RelationDto> links = new HashSet<>();
                                    List list = (List) value;
                                    for (Object o : list) {
                                        RelationDto relationDto = mapToObject((Map) o, RelationDto.class);
                                        links.add(relationDto);
                                    }
                                    field.setAccessible(true);
                                    field.set(t, links);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            });


            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将基本类型统一成包装类
     *
     * @param clazz
     * @return
     */
    private static Class formatClass(Class clazz) {

        if (clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)) {
            return Long.class;
        }
        if (clazz.equals(Long.TYPE)) {
            return Long.class;
        }
        if (clazz.equals(Character.TYPE)) {
            return Character.class;
        }
        if (clazz.equals(Byte.TYPE)) {
            return Byte.class;
        }
        if (clazz.equals(Short.TYPE)) {
            return Short.class;
        }

        if (clazz.equals(Float.TYPE) || clazz.equals(Float.class)) {
            return Double.class;
        }
        if (clazz.equals(Double.TYPE)) {
            return Double.class;
        }
        if (clazz.equals(Boolean.TYPE)) {
            return Boolean.class;
        }

        return clazz;
    }

    /**
     * 检测是否为父类
     *
     * @param parent
     * @param child
     * @return
     */
    private static boolean isSupperclass(Class parent, Class child) {

        Class superClass = null;
        Class[] interfaces = null;
        while ((superClass = child.getSuperclass()) != null) {

            interfaces = child.getInterfaces();

            if (interfaces != null) {
                for (Class anInterface : interfaces) {
                    if (anInterface.equals(parent)) {
                        return true;
                    }
                }
            }

            if (superClass.equals(parent)) {
                return true;
            }
            child = superClass;
        }

        return false;
    }

    public static String getKeyName(String key) {
        return key.replaceAll("_", "").toLowerCase();
    }

}
