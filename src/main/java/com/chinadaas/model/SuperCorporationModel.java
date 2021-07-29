package com.chinadaas.model;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.TargetType;
import com.chinadaas.commons.type.NodeType;
import com.chinadaas.component.wrapper.LinkWrapper;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.component.wrapper.PathWrapper;
import com.chinadaas.entity.TwoNodesEntity;
import com.google.common.collect.Sets;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团模型
 * @createTime 2021.07.28
 */
public class SuperCorporationModel {

    /**
     * 输入企业唯一标识
     */
    private final String currentQueryId;

    private String parent2SourceRelation;

    /**
     * 输入企业
     */
    private NodeWrapper sourceNode;

    /**
     * 母公司
     */
    private NodeWrapper parentNode;

    /**
     * 最终控股股东
     */
    private NodeWrapper finCtrlNode;

    /**
     * 最终控股股东到母公司的持股占比
     */
    private String ctrl2ParentCgzb;

    /**
     * 最终控股股东到母公司的路径
     */
    private PathWrapper ctrl2ParentPath;

    /**
     * 最终控股股东到输入企业的持股占比
     */
    private String ctrl2SourceCgzb;

    /**
     * 输入企业到最终控股股东的路径
     */
    private PathWrapper ctrl2SourcePath;

    /**
     * 输入企业到母公司的持股占比
     */
    private String parent2SourceCgzb;

    /**
     * 输入企业到母公司的路径
     */
    private PathWrapper parent2SourcePath;

    private ModelStatus resultStatus;

    public SuperCorporationModel(String currentQueryId) {
        this.currentQueryId = currentQueryId;
        this.resultStatus = ModelStatus.NO_RESULT;
    }

    /**
     * 生成输入企业到母公司的路径和控股比例
     *
     * @param twoNodesUseInvList
     * @param twoNodesUseGroupParentList
     */
    public void calSourceToParent(List<TwoNodesEntity> twoNodesUseInvList,
                                  List<TwoNodesEntity> twoNodesUseGroupParentList) {

        PathWrapper parent2SourcePath = new PathWrapper();
        BigDecimal tempInvCgzb = calTotalCgzb(twoNodesUseInvList, parent2SourcePath);
        BigDecimal tempGroupCgzb = calTotalCgzb(twoNodesUseGroupParentList, parent2SourcePath);

        BigDecimal totalCgzb;
        if (0 == tempInvCgzb.compareTo(new BigDecimal("0.0"))) {
            totalCgzb = tempGroupCgzb;
        } else if (0 == tempGroupCgzb.compareTo(new BigDecimal("0.0"))) {
            totalCgzb = tempInvCgzb;
        } else {
            totalCgzb = tempInvCgzb.multiply(tempGroupCgzb);
        }

        this.parent2SourceRelation = calParent2SourceRelation(parent2SourcePath);
        this.parent2SourcePath = parent2SourcePath;
        this.parent2SourceCgzb = totalCgzb.toString();
        this.resultStatus = ModelStatus.HAVE_RESULT;

    }

    /**
     * 不存在母公司，存在最终控股股东
     *
     * @param controlPathList
     */
    public void sourceToControlNoParent(List<TwoNodesEntity> controlPathList) {
        PathWrapper ctrl2SourcePath = new PathWrapper();
        BigDecimal totalCgzb = calTotalCgzb(controlPathList, ctrl2SourcePath);

        this.ctrl2SourcePath = ctrl2SourcePath;
        this.ctrl2SourceCgzb = totalCgzb.toString();
        this.resultStatus = ModelStatus.HAVE_RESULT;
    }

    /**
     * 当存在母公司，而不存在最终控股股东时，将母公司作为最终控股股东
     *
     * @param replaceGroupParentLink
     * @return
     */
    public void parentReplaceControl(Function<LinkWrapper, LinkWrapper> replaceGroupParentLink) {

        finCtrlNode = parentNode;

        Set<NodeWrapper> nodes = this.parent2SourcePath.getNodeWrappers();
        Set<LinkWrapper> links = this.parent2SourcePath.getLinkWrappers();
        Set<LinkWrapper> controlLinks = transferLinkIfContainGroupParent(links, replaceGroupParentLink);

        PathWrapper ctrl2SourcePath = new PathWrapper();
        ctrl2SourcePath.setNodeWrappers(nodes);
        ctrl2SourcePath.setLinkWrappers(controlLinks);

        this.ctrl2SourcePath = ctrl2SourcePath;
        this.ctrl2SourceCgzb = this.parent2SourceCgzb;
        this.resultStatus = ModelStatus.HAVE_RESULT;
    }

    /**
     * 同时存在母公司和最终控股股东
     *
     * @param sourceToControlPathList
     * @param parentToControlPathList
     * @param parentType
     * @return
     */
    public void sourceToControlHaveParent(List<TwoNodesEntity> sourceToControlPathList,
                                          List<TwoNodesEntity> parentToControlPathList,
                                          String parentType) {

        PathWrapper ctrl2ParentPath = new PathWrapper();
        PathWrapper ctrl2SourcePath = new PathWrapper();
        BigDecimal ctrl2ParentCgzb;
        BigDecimal ctrl2SourceCgzb;

        ctrl2ParentCgzb = calTotalCgzb(parentToControlPathList, ctrl2ParentPath);

        // 母公司是上市披露公司
        if (TargetType.DISCLOSURE.toString().equals(parentType)) {

            // merge path
            Set<NodeWrapper> nodes = ctrl2SourcePath.getNodeWrappers();
            Set<LinkWrapper> links = ctrl2SourcePath.getLinkWrappers();
            nodes.addAll(this.parent2SourcePath.getNodeWrappers());
            nodes.addAll(ctrl2ParentPath.getNodeWrappers());
            links.addAll(this.parent2SourcePath.getLinkWrappers());
            links.addAll(ctrl2ParentPath.getLinkWrappers());
            ctrl2SourcePath.setNodeWrappers(nodes);
            ctrl2SourcePath.setLinkWrappers(links);

            // cal cgzb
            BigDecimal source2ParentCgzb = new BigDecimal(this.parent2SourceCgzb);
            if (0 == ctrl2ParentCgzb.compareTo(new BigDecimal("0.0"))) {
                ctrl2SourceCgzb = source2ParentCgzb;
            } else if (0 == source2ParentCgzb.compareTo(new BigDecimal("0.0"))) {
                ctrl2SourceCgzb = ctrl2ParentCgzb;
            } else {
                ctrl2SourceCgzb = ctrl2ParentCgzb.multiply(source2ParentCgzb);
            }

        } else {
            ctrl2SourceCgzb = calTotalCgzb(sourceToControlPathList, ctrl2SourcePath);
        }


        this.ctrl2ParentCgzb = ctrl2ParentCgzb.toString();
        this.ctrl2SourceCgzb = ctrl2SourceCgzb.toString();
        this.ctrl2ParentPath = ctrl2ParentPath;
        this.ctrl2SourcePath = ctrl2SourcePath;
        this.resultStatus = ModelStatus.HAVE_RESULT;

    }

    public void setSourceProperty(NodeWrapper sourceNode) {
        this.sourceNode = sourceNode;
    }

    public void setParentProperty(NodeWrapper parentNode) {
        this.parentNode = parentNode;
    }

    public void setFinCtrlProperty(NodeWrapper finCtrlNode) {
        this.finCtrlNode = finCtrlNode;
    }

    public String getCurrentQueryId() {
        return currentQueryId;
    }

    public Map<String, Object> getSourceProperty() {
        if (Objects.nonNull(sourceNode)) {
            return sourceNode.getProperties();
        }
        return null;
    }

    public Map<String, Object> getParentProperty() {
        if (Objects.nonNull(parentNode)) {
            return parentNode.getProperties();
        }
        return null;
    }

    public Map<String, Object> getFinCtrlProperty() {
        if (Objects.nonNull(finCtrlNode)) {
            return finCtrlNode.getProperties();
        }
        return null;
    }

    public String getCtrl2ParentCgzb() {
        return ctrl2ParentCgzb;
    }

    public PathWrapper getCtrl2ParentPath() {
        return ctrl2ParentPath;
    }

    public String getCtrl2SourceCgzb() {
        return ctrl2SourceCgzb;
    }

    public PathWrapper getCtrl2SourcePath() {
        return ctrl2SourcePath;
    }

    public String getParent2SourceCgzb() {
        return parent2SourceCgzb;
    }

    public PathWrapper getParent2SourcePath() {
        return parent2SourcePath;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    /**
     * 计算两点之间持股占比
     * zs: 违反了srp原则，在此处merge了所有path
     *
     * @param twoNodesList
     * @param path
     * @return
     */
    private BigDecimal calTotalCgzb(List<TwoNodesEntity> twoNodesList, PathWrapper path) {
        Set<NodeWrapper> nodes = path.getNodeWrappers();
        Set<LinkWrapper> links = path.getLinkWrappers();

        BigDecimal tempCgzb = new BigDecimal("0.0");
        if (!CollectionUtils.isEmpty(twoNodesList)) {
            for (TwoNodesEntity twoNodesEntity : twoNodesList) {
                PathWrapper twoNodesPath = twoNodesEntity.getTwoNodesPath();
                nodes.addAll(twoNodesPath.getNodeWrappers());
                links.addAll(twoNodesPath.getLinkWrappers());

                BigDecimal twoNodesCgzb = new BigDecimal(String.valueOf(twoNodesEntity.getTwoNodesCgzb()));
                tempCgzb = tempCgzb.add(twoNodesCgzb);
            }
        }
        return tempCgzb;
    }

    /**
     * 将母公司转换成最终控股股东时，处理上市披露算法中所用到的groupparent边
     *
     * @param links
     * @param replaceGroupParentLink
     */
    private Set<LinkWrapper> transferLinkIfContainGroupParent(Set<LinkWrapper> links,
                                                              Function<LinkWrapper, LinkWrapper> replaceGroupParentLink) {
        Set<LinkWrapper> controlLinks = Sets.newHashSet();

        for (LinkWrapper link : links) {
            LinkWrapper controlLink = new LinkWrapper();

            final int GROUP_PARENT = 13;
            if (link.getType() == GROUP_PARENT) {
                LinkWrapper resultLink = replaceGroupParentLink.apply(link);
                // zs: 这里还是有可能混入groupparent边
                if (Objects.isNull(resultLink)) {
                    BeanUtils.copyProperties(link, controlLink);
                } else {
                    controlLink = resultLink;
                }
            } else {
                BeanUtils.copyProperties(link, controlLink);
            }

            controlLinks.add(controlLink);
        }

        return controlLinks;
    }

    private String calParent2SourceRelation(PathWrapper source2ParentPath) {
        String defaultRelation = "间接";
        String sourceId = sourceNode.getEntId();
        String parentId = parentNode.getEntId();

        if (Objects.equals(sourceId, parentId)) {
            return "直接";
        }

        Set<NodeWrapper> nodeWrappers = source2ParentPath.getNodeWrappers();
        NodeWrapper sourceNode = nodeWrappers.stream()
                .filter(nodeWrapper -> sourceId.equals(nodeWrapper.getEntId()))
                .findFirst()
                .orElse(null);
        NodeWrapper parentNode = nodeWrappers.stream()
                .filter(nodeWrapper -> parentId.equals(nodeWrapper.getEntId()))
                .findFirst()
                .orElse(null);

        Set<LinkWrapper> linkWrappers = source2ParentPath.getLinkWrappers();
        Optional<LinkWrapper> optional = linkWrappers.stream()
                .filter(
                        linkWrapper -> linkWrapper.getFrom() == parentNode.getId()
                                && linkWrapper.getTo() == sourceNode.getId()
                )
                .findFirst();

        if (optional.isPresent()) {
            return "直接";
        }

        return defaultRelation;
    }

    public String getEntId() {
        if (Objects.nonNull(sourceNode)) {
            return sourceNode.getEntId();
        }
        return "";
    }

    public String getEntName() {
        if (Objects.nonNull(sourceNode)) {
            return sourceNode.getEntName();
        }
        return "";
    }

    public String getFinCtrlId() {
        if (Objects.nonNull(finCtrlNode)) {
            int nodeType = finCtrlNode.getType();
            if (NodeType.ENT == nodeType) {
                return finCtrlNode.getEntId();
            }
            return finCtrlNode.getZsId();
        }
        return "";
    }

    public String getFinCtrlName() {
        if (Objects.nonNull(finCtrlNode)) {
            return finCtrlNode.getName();
        }
        return "";
    }

    public String getParent2SourceRelation() {
        return parent2SourceRelation;
    }

    public String getParentId() {
        if (Objects.nonNull(parentNode)) {
            return parentNode.getEntId();
        }
        return "";
    }

    public String getParentName() {
        if (Objects.nonNull(parentNode)) {
            return parentNode.getEntName();
        }
        return "";
    }

    public String getParentRegno() {
        if (Objects.nonNull(parentNode)) {
            return parentNode.getRegNo();
        }
        return "";
    }

    public String getParentCreditcode() {
        if (Objects.nonNull(parentNode)) {
            return parentNode.getCreditCode();
        }
        return "";
    }

    public String getEmId() {
        if (Objects.nonNull(finCtrlNode)) {
            return finCtrlNode.getEmId();
        }
        return "";
    }
}
