package com.chinadaas.service;

import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.component.io.EntIdListHolder;
import com.chinadaas.component.io.EntIdListLoader;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author lawliet
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

    @Override
    public void discernSuperCorporation() {

        lockResource();

        preDoDiscern();

        doDiscern();

        postDoDiscern();

        releaseResource();

    }

    protected void lockResource() {
        if (BooleanUtils.isFalse(entIdListLoader.lock())) {
            throw new IllegalStateException("data processing, please do not repeat call");
        }

        entIdListLoader.loadEntIdList(resourcePath);
    }

    protected void preDoDiscern() {
        // sub class do something here
    }

    protected void postDoDiscern() {
        // sub class do something here
    }

    protected abstract void doDiscern();

    protected void releaseResource() {
        entIdListHolder.clear();
        recordHandler.clearAUTypeIncr();
        recordHandler.clearDelTypeIncr();

        entIdListLoader.unlock();
    }
}
