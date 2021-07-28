package com.chinadaas.model;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.DecisionEntity;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 决策权模型
 * @createTime 2021.07.01
 */
public class DecisionModel {

    private long length;

    private NodeWrapper decisionNode;

    private ModelStatus resultStatus;

    public DecisionModel() {
        this.resultStatus = ModelStatus.NO_RESULT;
    }

    /**
     * 按照规则筛选对输入点持有最大持股占比的节点
     *
     * @param resultList
     * @return
     */
    public DecisionModel screenMaxPropNode(List<DecisionEntity> resultList) {

        if (CollectionUtils.isEmpty(resultList)) {
            return this;
        }

        resultStatus = ModelStatus.HAVE_RESULT;

        if (1 == resultList.size()) {
            DecisionEntity decisionEntity = resultList.get(0);
            this.decisionNode = decisionEntity.getDecisionNode();
            this.length = decisionEntity.getLength();
            return this;
        }

        return doScreenMaxPropNode(resultList);
    }

    private DecisionModel doScreenMaxPropNode(List<DecisionEntity> resultList) {

        // 按照持股占比分组
        Map<Double, List<DecisionEntity>> groupByCgzb = resultList.stream()
                .collect(
                        Collectors.groupingBy(
                                DecisionEntity::getFinalCgzb,
                                Collectors.toList()
                        )
                );

        // 获取持股占比最大实体
        DecisionEntity maxCgzbEntity = resultList.stream()
                .max(Comparator.comparingDouble(DecisionEntity::getFinalCgzb))
                .orElseThrow(RuntimeException::new);

        Double maxCgzb = maxCgzbEntity.getFinalCgzb();
        List<DecisionEntity> maxCgzbEntities = groupByCgzb.get(maxCgzb);

        if (1 == maxCgzbEntities.size()) {
            DecisionEntity finalMaxCgzbEntity = maxCgzbEntities.get(0);
            this.decisionNode = finalMaxCgzbEntity.getDecisionNode();
            this.length = finalMaxCgzbEntity.getLength();
            return this;
        }

        // 获取最短路径实体
        DecisionEntity minLengthEntity = maxCgzbEntities.stream()
                .min(Comparator.comparingLong(DecisionEntity::getLength))
                .orElseThrow(RuntimeException::new);
        this.decisionNode = minLengthEntity.getDecisionNode();
        this.length = minLengthEntity.getLength();

        return this;
    }

    public NodeWrapper getDecisionNode() {
        return decisionNode;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    public long getLength() {
        return length;
    }
}
