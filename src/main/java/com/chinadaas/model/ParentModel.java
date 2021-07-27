package com.chinadaas.model;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.SourceEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author liubc
 * @version 1.0.0
 * @description 母公司模型
 * @createTime 2021.07.13
 */
@Slf4j
public class ParentModel {

    /**
     * 当前queryId
     */
    private final String currentQueryId;

    /**
     * 结果状态
     */
    private ModelStatus resultStatus;

    /**
     * 母公司点
     */
    private NodeWrapper parentNode;

    /**
     * 输入点
     */
    private NodeWrapper sourceNode;

    public ParentModel(String currentQueryId) {
        this.currentQueryId = currentQueryId;
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

    public boolean recordDecisionResult(DecisionModel decisionModel) {

        if (ModelStatus.NO_RESULT.equals(decisionModel.getResultStatus()))
            return true;

        this.resultStatus = ModelStatus.COMPLETE_RESULT;
        this.parentNode = decisionModel.getDecisionNode();
        return false;
    }

    /*
    * 单一大股东和上市披露绑定执行，故返回结果永远为true
    * */

    public boolean recordSingleShareHolder(SingleShareHolderModel singleShareHolderModel) {

        if (ModelStatus.NO_RESULT.equals(singleShareHolderModel.getResultStatus()))
            return true;

        this.resultStatus = ModelStatus.COMPLETE_RESULT;
        this.parentNode = singleShareHolderModel.getSingleShareHolderNode();
        return false;
    }

    public boolean recordListDisclosureResult(ListDisclosureModel listDisclosureModel) {

        if (ModelStatus.NO_RESULT.equals(listDisclosureModel.getResultStatus()))
            return true;

        this.resultStatus = ModelStatus.COMPLETE_RESULT;
        this.parentNode = listDisclosureModel.getListDisclosureNode();
        return true;
    }

    public String getCurrentQueryId() {
        return currentQueryId;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    public NodeWrapper getParentNode() {
        return parentNode;
    }

    public NodeWrapper getSourceNode() {
        return sourceNode;
    }

}
