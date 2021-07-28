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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private String currentQueryId;

    /**
     * 输入企业
     */
    private Map<String, Object> sourceProperty;

    /**
     * 母公司
     */
    private Map<String, Object> parentProperty;

    /**
     * 最终控股股东
     */
    private Map<String, Object> finCtrlProperty;

    /**
     * 母公司到最终控股股东的持股占比
     */
    private String parent2CtrlCgzb;

    /**
     * 母公司到最终控股股东的路径
     */
    private PathWrapper parent2CtrlPath;

    /**
     * 输入企业到最终控股股东的持股占比
     */
    private String source2CtrlCgzb;

    /**
     * 输入企业到最终控股股东的路径
     */
    private PathWrapper source2CtrlPath;

    /**
     * 输入企业到母公司的持股占比
     */
    private String source2ParentCgzb;

    /**
     * 输入企业到母公司的路径
     */
    private PathWrapper source2ParentPath;

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
    public SuperCorporationModel calSourceToParent(List<TwoNodesEntity> twoNodesUseInvList,
                                                   List<TwoNodesEntity> twoNodesUseGroupParentList) {

        PathWrapper source2ParentPath = new PathWrapper();
        BigDecimal tempInvCgzb = calTotalCgzb(twoNodesUseInvList, source2ParentPath);
        BigDecimal tempGroupCgzb = calTotalCgzb(twoNodesUseGroupParentList, source2ParentPath);

        BigDecimal totalCgzb;
        if (tempInvCgzb.compareTo(new BigDecimal(0.0)) == 0) {
            totalCgzb = tempGroupCgzb;
        } else if (tempGroupCgzb.compareTo(new BigDecimal(0.0)) == 0) {
            totalCgzb = tempInvCgzb;
        } else {
            totalCgzb = tempInvCgzb.multiply(tempGroupCgzb);
        }

        this.source2ParentPath = source2ParentPath;
        this.source2ParentCgzb = totalCgzb.toString();
        this.resultStatus = ModelStatus.HAVE_RESULT;

        return this;
    }

    /**
     * 不存在母公司，存在最终控股股东
     *
     * @param controlPathList
     */
    public SuperCorporationModel sourceToControlNoParent(List<TwoNodesEntity> controlPathList) {
        PathWrapper source2CtrlPath = new PathWrapper();
        BigDecimal totalCgzb = calTotalCgzb(controlPathList, source2CtrlPath);

        this.source2CtrlPath = source2CtrlPath;
        this.source2CtrlCgzb = totalCgzb.toString();
        this.resultStatus = ModelStatus.HAVE_RESULT;

        return this;
    }

    /**
     * 当存在母公司，而不存在最终控股股东时，将母公司作为最终控股股东
     *
     * @param replaceGroupParentLink
     * @return
     */
    public SuperCorporationModel parentReplaceControl(Function<LinkWrapper, LinkWrapper> replaceGroupParentLink) {

        finCtrlProperty = parentProperty;

        Set<NodeWrapper> nodes = this.source2ParentPath.getNodeWrappers();
        Set<LinkWrapper> links = this.source2ParentPath.getLinkWrappers();
        Set<LinkWrapper> controlLinks = transferLinkIfContainGroupParent(links, replaceGroupParentLink);

        PathWrapper source2CtrlPath = new PathWrapper();
        source2CtrlPath.setNodeWrappers(nodes);
        source2CtrlPath.setLinkWrappers(controlLinks);

        this.source2CtrlPath = source2CtrlPath;
        this.source2CtrlCgzb = this.source2ParentCgzb;
        this.resultStatus = ModelStatus.HAVE_RESULT;

        return this;
    }

    /**
     * 同时存在母公司和最终控股股东
     *
     * @param sourceToControlPathList
     * @param parentToControlPathList
     * @param parentType
     * @return
     */
    public SuperCorporationModel sourceToControlHaveParent(List<TwoNodesEntity> sourceToControlPathList,
                                                           List<TwoNodesEntity> parentToControlPathList,
                                                           String parentType) {
        PathWrapper parent2CtrlPath = new PathWrapper();
        PathWrapper source2CtrlPath = new PathWrapper();
        BigDecimal parent2CtrlCgzb;
        BigDecimal source2CtrlCgzb;

        parent2CtrlCgzb = calTotalCgzb(parentToControlPathList, parent2CtrlPath);

        // 母公司是上市披露公司
        if (TargetType.DISCLOSURE.toString().equals(parentType)) {

            // merge path
            Set<NodeWrapper> nodes = source2CtrlPath.getNodeWrappers();
            Set<LinkWrapper> links = source2CtrlPath.getLinkWrappers();
            nodes.addAll(this.source2ParentPath.getNodeWrappers());
            nodes.addAll(parent2CtrlPath.getNodeWrappers());
            links.addAll(this.source2ParentPath.getLinkWrappers());
            links.addAll(parent2CtrlPath.getLinkWrappers());
            source2CtrlPath.setNodeWrappers(nodes);
            source2CtrlPath.setLinkWrappers(links);

            // cal cgzb
            BigDecimal source2ParentCgzb = new BigDecimal(this.source2ParentCgzb);
            if (parent2CtrlCgzb.compareTo(new BigDecimal(0.0)) == 0) {
                source2CtrlCgzb = source2ParentCgzb;
            } else if (source2ParentCgzb.compareTo(new BigDecimal(0.0)) == 0) {
                source2CtrlCgzb = parent2CtrlCgzb;
            } else {
                source2CtrlCgzb = parent2CtrlCgzb.multiply(source2ParentCgzb);
            }

        } else {
            source2CtrlCgzb = calTotalCgzb(sourceToControlPathList, source2CtrlPath);
        }


        this.parent2CtrlCgzb = parent2CtrlCgzb.toString();
        this.source2CtrlCgzb = source2CtrlCgzb.toString();
        this.parent2CtrlPath = parent2CtrlPath;
        this.source2CtrlPath = source2CtrlPath;
        this.resultStatus = ModelStatus.HAVE_RESULT;

        return this;
    }

    public void setSourceProperty(NodeWrapper sourceNode) {
        this.sourceProperty = sourceNode.getProperties();
    }

    public void setParentProperty(NodeWrapper parentNode) {
        this.parentProperty = parentNode.getProperties();
    }

    public void setFinCtrlProperty(NodeWrapper finCtrlNode) {
        this.finCtrlProperty = finCtrlNode.getProperties();
    }

    public String getCurrentQueryId() {
        return currentQueryId;
    }

    public Map<String, Object> getSourceProperty() {
        return sourceProperty;
    }

    public Map<String, Object> getParentProperty() {
        return parentProperty;
    }

    public Map<String, Object> getFinCtrlProperty() {
        return finCtrlProperty;
    }

    public String getParent2CtrlCgzb() {
        return parent2CtrlCgzb;
    }

    public PathWrapper getParent2CtrlPath() {
        return parent2CtrlPath;
    }

    public String getSource2CtrlCgzb() {
        return source2CtrlCgzb;
    }

    public PathWrapper getSource2CtrlPath() {
        return source2CtrlPath;
    }

    public String getSource2ParentCgzb() {
        return source2ParentCgzb;
    }

    public PathWrapper getSource2ParentPath() {
        return source2ParentPath;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    /**
     * 计算两点之间持股占比
     * todo: 违反了srp原则，在此处merge了所有path
     *
     * @param twoNodesList
     * @param path
     * @return
     */
    private BigDecimal calTotalCgzb(List<TwoNodesEntity> twoNodesList, PathWrapper path) {
        Set<NodeWrapper> nodes = path.getNodeWrappers();
        Set<LinkWrapper> links = path.getLinkWrappers();

        BigDecimal tempCgzb = new BigDecimal(0.0);
        if (!CollectionUtils.isEmpty(twoNodesList)) {
            for (TwoNodesEntity twoNodesEntity : twoNodesList) {
                PathWrapper twoNodesPath = twoNodesEntity.getTwoNodesPath();
                nodes.addAll(twoNodesPath.getNodeWrappers());
                links.addAll(twoNodesPath.getLinkWrappers());

                BigDecimal twoNodesCgzb = new BigDecimal(twoNodesEntity.getTwoNodesCgzb());
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
                // fixme: 这里还是有可能混入groupparent边
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

}
