package com.chinadaas.service;

/**
 * @author liubc
 * @version 1.0.0
 * @description 节点操作
 * @createTime 2021.07.23
 */
public interface NodeOperationService {

    /**
     * 节点划组
     *
     * @param entId
     */
    void nodeFix(String entId);

    /**
     * 企业是否在营，true表示在营，false表示非在营
     *
     * @param entId
     * @return
     */
    boolean managementStatus(String entId);
}
