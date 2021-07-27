package com.chinadaas.model;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.ListDisclosureEntity;

import java.util.Objects;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 上市披露模型
 * @createTime 2021.07.13
 */
public class ListDisclosureModel {

    private NodeWrapper listDisclosureNode;

    private ModelStatus resultStatus;

    public ListDisclosureModel() {
        this.resultStatus = ModelStatus.NO_RESULT;
    }

    public ListDisclosureModel convertResult(ListDisclosureEntity listDisclosureEntity) {

        if (Objects.isNull(listDisclosureEntity)) {
            return this;
        }

        this.resultStatus = ModelStatus.HAVE_RESULT;
        this.listDisclosureNode = listDisclosureEntity.getListDisclosureNode();
        return this;
    }

    public NodeWrapper getListDisclosureNode() {
        return listDisclosureNode;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }
}
