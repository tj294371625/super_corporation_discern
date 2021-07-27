package com.chinadaas.common.constant;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 结果状态
 * @createTime 2021.07.02
 */
public enum ModelStatus {
    /**
     * 无结果
     */
    NO_RESULT,

    /**
     * 存在结果
     */
    HAVE_RESULT,

    /**
     * 仅存在输入点
     */
    SOURCE_ONLY,

    /**
     * 完整结果
     */
    COMPLETE_RESULT;

    public boolean noEquals(ModelStatus modelStatus) {
        return !this.equals(modelStatus);
    }
}
