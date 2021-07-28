package com.chinadaas.model;

import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.SingleShareHolderEntity;

import java.util.Objects;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 单一大股东模型
 * @createTime 2021.07.13
 */
public class SingleShareHolderModel {

    /**
     * 单一大股东到输入点间长度默认为1
     */
    private long length;

    private NodeWrapper singleShareHolderNode;

    private ModelStatus resultStatus;

    public SingleShareHolderModel() {
        this.length = 1L;
        this.resultStatus = ModelStatus.NO_RESULT;
    }

    public SingleShareHolderModel convertResult(SingleShareHolderEntity singleShareHolderEntity) {

        if (Objects.isNull(singleShareHolderEntity)) {
            return this;
        }

        this.resultStatus = ModelStatus.HAVE_RESULT;
        this.singleShareHolderNode = singleShareHolderEntity.getSingleShareHolderNode();
        return this;
    }

    public NodeWrapper getSingleShareHolderNode() {
        return singleShareHolderNode;
    }

    public ModelStatus getResultStatus() {
        return resultStatus;
    }

    public long getLength() {
        return length;
    }
}
