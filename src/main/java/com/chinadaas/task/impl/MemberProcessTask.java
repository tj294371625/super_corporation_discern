package com.chinadaas.task.impl;

import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.io.EntIdListLoader;
import com.chinadaas.entity.old.*;
import com.chinadaas.model.*;
import com.chinadaas.service.MemberService;
import com.chinadaas.service.SuperCorporationService;
import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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

    private final Executor parallelExecutor;
    private final RecordHandler recordHandler;
    private final EntIdListLoader entIdListLoader;
    private final SuperCorporationService superCorporationService;
    private final MemberService memberService;

    @Autowired
    public MemberProcessTask(@Qualifier("parallelExecutor") Executor parallelExecutor,
                             RecordHandler recordHandler,
                             EntIdListLoader entIdListLoader,
                             SuperCorporationService superCorporationService,
                             MemberService memberService) {

        this.parallelExecutor = parallelExecutor;
        this.recordHandler = recordHandler;
        this.entIdListLoader = entIdListLoader;
        this.superCorporationService = superCorporationService;
        this.memberService = memberService;
    }

    private MemberCalTask memberCalTask;
    private DataStorageTask dataStorageTask;

    @PostConstruct
    public void init() {
        memberCalTask = new MemberCalTask();
        dataStorageTask = new DataStorageTask();
    }

    @Override
    public void run() {
        log.info("member process task start run...");
        long startTime = TimeUtils.startTime();

        Set<String> parentIds = superCorporationService.extraParentIds();
        entIdListLoader.reloadEntIdList(parentIds);

        final Consumer<String> processTask = (entId) -> {
            SuperMemberModel superMemberModel = new SuperMemberModel(entId);
            memberCalTask.cal(superMemberModel);
            dataStorageTask.run(superMemberModel);
        };

        parallelExecutor.execute("member process task", processTask);

        log.info("end the member process task, spend time: [{}ms]", TimeUtils.endTime(startTime));
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
            discernAndMajorPersonStorage(superMemberModel);
            discernAndStaffStorage(superMemberModel);
            discernLegalOut(superMemberModel);
            controlPersonLegal(superMemberModel);
            personOutControl(superMemberModel);
            majorPerson(superMemberModel);
            staff(superMemberModel);
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

        private void discernAndStaffStorage(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> discernAndStaffRecords = superMemberModel.discernAndStaffModelRecords();
            memberService.addDiscernAndStaff(discernAndStaffRecords);
            List<StaffAndParentCommonInfo> staffAndParentCommonInfos
                    = AssistantUtils.mapListToList(discernAndStaffRecords, StaffAndParentCommonInfo.class);
            recordHandler.recordDiscernAndStaff(staffAndParentCommonInfos);
        }

        private void discernLegalOut(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> discernLegalOutRecords = superMemberModel.discernLegalOutModelRecords();
            memberService.addDiscernLegalOut(discernLegalOutRecords);
            List<StaffAndParentCommonInfo> staffAndParentCommonInfos
                    = AssistantUtils.mapListToList(discernLegalOutRecords, StaffAndParentCommonInfo.class);
            recordHandler.recordDiscernLegalOut(staffAndParentCommonInfos);
        }

        private void controlPersonLegal(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> controlPersonLegalRecords = superMemberModel.controlPersonLegalModelRecords();
            memberService.addControlPersonLegal(controlPersonLegalRecords);
            List<BasePersonInfo> basePersonInfos
                    = AssistantUtils.mapListToList(controlPersonLegalRecords, BasePersonInfo.class);
            recordHandler.recordControlPersonLegal(basePersonInfos);
        }

        private void personOutControl(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> personOutControlRecords = superMemberModel.personOutControlModelRecords();
            memberService.addPersonOutControl(personOutControlRecords);
            List<PersonOutControlInfo> personOutControlInfos
                    = AssistantUtils.mapListToList(personOutControlRecords, PersonOutControlInfo.class);
            recordHandler.recordPersonOutControl(personOutControlInfos);
        }

        private void majorPerson(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> majorPersonRecords = superMemberModel.majorPersonModelRecords();
            memberService.addMajorPerson(majorPersonRecords);
            List<MajorInvPersonInfo> majorInvPersonInfos
                    = AssistantUtils.mapListToList(majorPersonRecords, MajorInvPersonInfo.class);
            recordHandler.recordMajorPerson(majorInvPersonInfos);
        }

        private void staff(SuperMemberModel superMemberModel) {
            List<Map<String, Object>> staffRecords = superMemberModel.staffModelRecords();
            memberService.addStaff(staffRecords);
            List<StaffPerson> staffPeople = AssistantUtils.mapListToList(staffRecords, StaffPerson.class);
            recordHandler.recordStaff(staffPeople);
        }

    }

}
