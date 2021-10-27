package com.chinadaas.common.util;

import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.service.ChainOperationService;
import com.chinadaas.service.SuperCorporationService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 名单优化
 * @createTime 2021.08.16
 */
@Slf4j
@Component
public class EntIdListOptHandler {

    private final RecordHandler recordHandler;
    private final EntIdListLoader entIdListLoader;
    private final ChainOperationService chainOperationService;
    private final SuperCorporationService superCorporationService;

    @Autowired
    public EntIdListOptHandler(RecordHandler recordHandler,
                               EntIdListLoader entIdListLoader,
                               ChainOperationService chainOperationService,
                               SuperCorporationService superCorporationService) {

        this.recordHandler = recordHandler;
        this.entIdListLoader = entIdListLoader;
        this.chainOperationService = chainOperationService;
        this.superCorporationService = superCorporationService;
    }

    public void replacePreWithParentFixEntIds() {
        Set<String> parentFixEntIds = chainOperationService.obtainParentFixEntIds();
        entIdListLoader.reloadEntIdList(parentFixEntIds);
    }

    public void replacePreWithFinCtrlEntIds() {
        Set<String> finCtrlEntIds = chainOperationService.obtainFinCtrlEntIds();
        Set<String> circularEntIds = chainOperationService.obtainCircularEntIds();
        finCtrlEntIds.addAll(circularEntIds);
        entIdListLoader.reloadEntIdList(finCtrlEntIds);
    }

    public void replacePreWithFinCtrlFixEntIds() {
        Set<String> finCtrlFixEntIds = chainOperationService.obtainFinCtrlFixEntIds();
        entIdListLoader.reloadEntIdList(finCtrlFixEntIds);
    }

    public void replacePreWithSuperCorEntIds() {
        Set<String> parentFixEntIds = chainOperationService.obtainParentFixEntIds();
        Set<String> finCtrlFixEntIds = chainOperationService.obtainFinCtrlFixEntIds();
        Set<String> allEntIds = Sets.newHashSet();
        allEntIds.addAll(parentFixEntIds);
        allEntIds.addAll(finCtrlFixEntIds);
        entIdListLoader.reloadEntIdList(allEntIds);
    }

    public void replacePreWithMembersEntIds() {
        Set<String> parentIds = superCorporationService.extraParentIds();
        entIdListLoader.reloadEntIdList(parentIds);
    }

    public void replacePreWithAUIncrEntIds() {
        // zs: 获取待新增的parent，记录parentid，重新生成成员
        Set<String> auTypeIncrSet = recordHandler.obtainAUTypeIncrSet();
        Set<String> auParentIds = superCorporationService.queryParentIdsByEntIds(auTypeIncrSet);
        entIdListLoader.reloadEntIdList(auParentIds);
    }

    public void replacePreWithIncrFixEntIds() {
        Set<String> auTypeIncrSet = recordHandler.obtainAUTypeIncrSet();
        entIdListLoader.reloadEntIdList(auTypeIncrSet);
    }

    public void replacePreWithIncrDelEntIds() {
        Set<String> delTypeIncrSet = recordHandler.obtainDelTypeIncrSet();
        entIdListLoader.reloadEntIdList(delTypeIncrSet);
    }

}
