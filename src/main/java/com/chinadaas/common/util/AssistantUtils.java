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
import com.chinadaas.entity.old.BaseEntInfo;
import com.chinadaas.entity.old.ParentAndMajorInvPersonInfo;
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
        List<String> recordList = baseEntInfos.stream().map(
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
        ).collect(Collectors.toList());

        return recordList;
    }

    public static void discernAndMajorPersonRecord(List<ParentAndMajorInvPersonInfo> parentAndMajorInvPersonInfos) {
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
