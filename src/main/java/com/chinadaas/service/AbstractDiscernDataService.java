package com.chinadaas.service;

import com.chinadaas.common.utils.RecordHandler;
import com.chinadaas.component.io.EntIdListHolder;
import com.chinadaas.component.io.EntIdListLoader;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author liubc
 * @version 1.0.0
 * @description 模版
 * @createTime 2021.07.21
 */
public abstract class AbstractDiscernDataService implements DiscernDataService {

    @Value("${data.path.source}")
    private String resourcePath;

    protected EntIdListLoader entIdListLoader;
    protected EntIdListHolder entIdListHolder;
    protected RecordHandler recordHandler;

    public void discernSuperCorporation() {
        preHandle();

        init();

        doDiscern();

        postHandle();
    }

    protected void preHandle() {
        if (BooleanUtils.isFalse(entIdListLoader.lock())) {
            throw new IllegalStateException("data processing, please do not repeat call");
        }

        entIdListLoader.loadEntIdList(resourcePath);
    }

    protected void init() {
        // subclass do something
    }

    protected abstract void doDiscern();

    protected void postHandle() {
        entIdListHolder.clear();
        recordHandler.clearMissRecords();
        recordHandler.clearAUTypeIncr();

        entIdListLoader.unlock();
    }
}
