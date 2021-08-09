package com.chinadaas.task.impl;

import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.entity.old.BaseEntInfo;
import com.chinadaas.entity.old.ParentAndMajorInvPersonInfo;
import com.chinadaas.model.*;
import com.chinadaas.service.MemberService;
import com.chinadaas.service.SuperCorporationService;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 执行成员业务逻辑
 * @createTime 2021.08.03
 */
@Slf4j
@Order(3)
@Component
public class MemberProcessTask implements FullTask {

    private final RecordHandler recordHandler;
    private final EntIdListLoader entIdListLoader;
    private final SuperCorporationService superCorporationService;
    private final MemberService memberService;

    @Autowired
    public MemberProcessTask(RecordHandler recordHandler,
                             EntIdListLoader entIdListLoader,
                             SuperCorporationService superCorporationService,
                             MemberService memberService) {

        this.recordHandler = recordHandler;
        this.entIdListLoader = entIdListLoader;
        this.superCorporationService = superCorporationService;
        this.memberService = memberService;
    }

    private ParentIdExtraTask parentIdExtraTask;

    @PostConstruct
    public void init() {
        parentIdExtraTask = new ParentIdExtraTask();
    }

    @Override
    public void run() {
        parentIdExtraTask.run();

    }

    private class ParentIdExtraTask {

        public void run() {
            Set<String> parentIds = superCorporationService.extraParentIds();
            entIdListLoader.reloadEntIdList(parentIds);
        }
    }

    private class MemberCalTask {

        public void cal(SuperMemberModel superMemberModel) {
            String currentQueryId = superMemberModel.getCurrentQueryId();

            List<MemberModel> memberModels = memberService.obtainMembers(currentQueryId);
            List<DiscernAndMajorPersonModel> discernAndMajorPersonModels = memberService.obtainDiscernAndMajorPersons(currentQueryId);
            List<DiscernAndStaffModel> discernAndStaffModels = memberService.obtainDiscernAndStaffs(currentQueryId);
            DiscernLegalOutModel discernLegalOutModel = memberService.obtainDiscernLegalOut(currentQueryId);
            ControlPersonLegalModel controlPersonLegalModel = memberService.obtainControlPersonLegal(currentQueryId);
            PersonOutControlModel personOutControlModel = memberService.obtainPersonOutControl(currentQueryId);
            MajorPersonModel majorPersonModel = memberService.obtainMajorPerson(currentQueryId);
            StaffModel staffModel = memberService.obtainStaff(currentQueryId);

            superMemberModel.setMemberModels(memberModels);
            superMemberModel.setDiscernAndMajorPersonModels(discernAndMajorPersonModels);
            superMemberModel.setDiscernAndStaffModels(discernAndStaffModels);
            superMemberModel.setDiscernLegalOutModel(discernLegalOutModel);
            superMemberModel.setControlPersonLegalModel(controlPersonLegalModel);
            superMemberModel.setPersonOutControlModel(personOutControlModel);
            superMemberModel.setMajorPersonModel(majorPersonModel);
            superMemberModel.setStaffModel(staffModel);
        }

    }

    private class DataStorageTask {

        public void run(SuperMemberModel superMemberModel) {
            memberStorage(superMemberModel);
        }

        private void memberStorage(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> memberRecords = superMemberModel.memberRecords();
            // 写入数据库
            memberService.addMembers(memberRecords);
            // 写入文件
            List<BaseEntInfo> baseEntInfos = AssistantUtils.mapListToList(memberRecords, BaseEntInfo.class);
            recordHandler.recordMembers(baseEntInfos);
        }

        private void discernAndMajorPersonStorage(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> discernAndMajorPersonRecords = superMemberModel.discernAndMajorPersonRecords();
            memberService.addDiscernAndMajorPerson(discernAndMajorPersonRecords);
            List<ParentAndMajorInvPersonInfo> parentAndMajorInvPersonInfos
                    = AssistantUtils.mapListToList(discernAndMajorPersonRecords, ParentAndMajorInvPersonInfo.class);
            recordHandler.recordDiscernAndMajorPerson(parentAndMajorInvPersonInfos);
        }

    }

}
