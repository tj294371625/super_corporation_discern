package com.chinadaas.model;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.common.constant.ModelType;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.SourceEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 链路模型
 * @createTime 2021.07.13
 */
@Slf4j
public class ChainModel {

    /**
     * 当前queryId
     */
    private final String currentQueryId;

    /**
     * 业务类型
     */
    private final ModelType businessType;

    /**
     * 结果状态
     */
    private ModelStatus resultStatus;

    /**
     * 目标点（母公司 or 最终控股股东）
     */
    private NodeWrapper targetNode;

    /**
     * 输入点
     */
    private NodeWrapper sourceNode;

    public ChainModel(String currentQueryId,
                      ModelType modelType) {

        this.currentQueryId = currentQueryId;
        this.businessType = modelType;
        this.resultStatus = ModelStatus.NO_RESULT;
    }

    public boolean recordSourceResult(SourceEntity sourceEntity) {

        if (Objects.nonNull(sourceEntity)) {
            this.resultStatus = ModelStatus.SOURCE_ONLY;
            this.sourceNode = sourceEntity.getSourceNode();
            return true;
        }

        return false;
    }

    public boolean recordDecisionResult(DecisionModel decisionModel, ModelType businessType) {

        if (ModelStatus.NO_RESULT.equals(decisionModel.getResultStatus())) {
            // zs: 最终控股股东不需要修正，结束责任链执行
            return !ModelType.FIN_CTRL.equals(businessType);
        }

        this.resultStatus = ModelStatus.COMPLETE_RESULT;
        this.targetNode = decisionModel.getDecisionNode();
        return false;
    }

    public boolean recordSingleShareHolder(SingleShareHolderModel singleShareHolderModel) {

        if (ModelStatus.NO_RESULT.equals(singleShareHolderModel.getResultStatus()))
            return true;

        this.resultStatus = ModelStatus.COMPLETE_RESULT;
        this.targetNode = singleShareHolderModel.getSingleShareHolderNode();
        return false;
    }

    public boolean recordListDisclosureResult(ListDisclosureModel listDisclosureModel) {

        if (ModelStatus.NO_RESULT.equals(listDisclosureModel.getResultStatus()))
            return true;

        this.resultStatus = ModelStatus.COMPLETE_RESULT;
        this.targetNode = listDisclosureModel.getListDisclosureNode();
        return false;
    }

    public String getCurrentQueryId() {
        return currentQueryId;
    }

    public ModelType getBusinessType() {
        return businessType;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    public NodeWrapper getTargetNode() {
        return targetNode;
    }

    public NodeWrapper getSourceNode() {
        return sourceNode;
    }

}
