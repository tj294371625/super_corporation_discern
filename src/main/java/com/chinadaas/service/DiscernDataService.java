package com.chinadaas.service;

import com.chinadaas.common.constant.ImportMode;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团数据识别服务
 * @createTime 2021.07.02
 */
public interface DiscernDataService {

    /**
     * 集团识别
     */
    void discernSuperCorporation();

    boolean hit(ImportMode mode);
}
