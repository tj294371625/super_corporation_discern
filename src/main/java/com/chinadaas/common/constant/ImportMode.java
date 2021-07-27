package com.chinadaas.common.constant;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 倒入模式
 * @createTime 2021.07.20
 */
public enum ImportMode {

    /**
     * 增量模式
     */
    INCR_MODE,

    /**
     * 全量模式
     */
    FULL_MODE;

    public boolean noEquals(ImportMode importMode) {
        return !this.equals(importMode);
    }

}
