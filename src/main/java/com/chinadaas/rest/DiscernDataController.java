package com.chinadaas.rest;

import com.chinadaas.common.constant.ImportMode;
import com.chinadaas.common.request.CommonRequest;
import com.chinadaas.service.DiscernDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团派系识别入口
 * @createTime 2021.07.01
 */
@Slf4j
@RestController
public class DiscernDataController {

    private static final String DISCERN_URL = "/discern_data";

    private final List<DiscernDataService> discernServices;

    @Autowired
    public DiscernDataController(List<DiscernDataService> discernServices) {
        this.discernServices = discernServices;
    }

    @PostMapping(DISCERN_URL)
    public String discern(@RequestBody CommonRequest commonRequest) {

        String response = checkRequest(commonRequest);
        if (Objects.nonNull(response)) {
            return response;
        }

        ImportMode importMode = commonRequest.getMode();
        for (DiscernDataService discernService : discernServices) {
            if (discernService.hit(importMode)) {
                log.info("select [{}] importMode", importMode);
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(discernService::discernSuperCorporation);
            }
        }

        return "call success, start execute task";
    }

    private String checkRequest(CommonRequest commonRequest) {
        ImportMode importMode = commonRequest.getMode();

        if (Objects.isNull(importMode)) {
            return "call fail, importMode must not null";
        }

        if (ImportMode.INCR_MODE.noEquals(importMode)
                && ImportMode.FULL_MODE.noEquals(importMode)) {
            return "call fail, there is no such importMode";
        }

        return null;
    }

}
