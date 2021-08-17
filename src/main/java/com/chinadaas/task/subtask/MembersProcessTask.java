package com.chinadaas.task.subtask;

import com.chinadaas.common.util.AssistantUtils;
import com.chinadaas.common.util.RecordHandler;
import com.chinadaas.common.util.TimeUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.entity.old.*;
import com.chinadaas.model.*;
import com.chinadaas.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.08.03
 */
@Slf4j
@Component
public class MembersProcessTask {

    private final Executor parallelExecutor;
    private final RecordHandler recordHandler;
    private final MemberService memberService;

    @Autowired
    public MembersProcessTask(@Qualifier("parallelExecutor") Executor parallelExecutor,
                              RecordHandler recordHandler,
                              MemberService memberService) {

        this.parallelExecutor = parallelExecutor;
        this.recordHandler = recordHandler;
        this.memberService = memberService;
    }

    public void generateMembers() {
        log.info("member process task start run...");
        long startTime = TimeUtils.startTime();

        final Consumer<String> processTask = (entId) -> {
            SuperMemberModel superMemberModel = new SuperMemberModel(entId);
            calPartOfMembers(superMemberModel);
            storageMembers(superMemberModel);
        };

        parallelExecutor.execute("member process task", processTask);

        log.info("end the member process task, spend time: [{}ms]", TimeUtils.endTime(startTime));
    }

    private void calPartOfMembers(SuperMemberModel superMemberModel) {
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

    private void storageMembers(SuperMemberModel superMemberModel) {
        memberStorage(superMemberModel);
        discernAndMajorPersonStorage(superMemberModel);
        discernAndStaffStorage(superMemberModel);
        discernLegalOutStorage(superMemberModel);
        controlPersonLegalStorage(superMemberModel);
        personOutControlStorage(superMemberModel);
        majorPersonStorage(superMemberModel);
        staffStorage(superMemberModel);
    }

    private void memberStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> memberRecords = superMemberModel.memberRecords();
        if (CollectionUtils.isEmpty(memberRecords)) {
            return;
        }

        // 写入数据库
        memberService.addMembers(memberRecords);
        // 写入文件
        List<BaseEntInfo> baseEntInfos = AssistantUtils.mapListToList(memberRecords, BaseEntInfo.class);
        recordHandler.recordMembers(baseEntInfos);
    }

    private void discernAndMajorPersonStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> discernAndMajorPersonRecords = superMemberModel.discernAndMajorPersonRecords();
        if (CollectionUtils.isEmpty(discernAndMajorPersonRecords)) {
            return;
        }

        memberService.addDiscernAndMajorPerson(discernAndMajorPersonRecords);
        List<ParentAndMajorInvPersonInfo> parentAndMajorInvPersonInfos
                = AssistantUtils.mapListToList(discernAndMajorPersonRecords, ParentAndMajorInvPersonInfo.class);
        recordHandler.recordDiscernAndMajorPerson(parentAndMajorInvPersonInfos);
    }

    private void discernAndStaffStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> discernAndStaffRecords = superMemberModel.discernAndStaffModelRecords();
        if (CollectionUtils.isEmpty(discernAndStaffRecords)) {
            return;
        }

        memberService.addDiscernAndStaff(discernAndStaffRecords);
        List<StaffAndParentCommonInfo> staffAndParentCommonInfos
                = AssistantUtils.mapListToList(discernAndStaffRecords, StaffAndParentCommonInfo.class);
        recordHandler.recordDiscernAndStaff(staffAndParentCommonInfos);
    }

    private void discernLegalOutStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> discernLegalOutRecords = superMemberModel.discernLegalOutModelRecords();
        if (CollectionUtils.isEmpty(discernLegalOutRecords)) {
            return;
        }

        memberService.addDiscernLegalOut(discernLegalOutRecords);
        List<StaffAndParentCommonInfo> staffAndParentCommonInfos
                = AssistantUtils.mapListToList(discernLegalOutRecords, StaffAndParentCommonInfo.class);
        recordHandler.recordDiscernLegalOut(staffAndParentCommonInfos);
    }

    private void controlPersonLegalStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> controlPersonLegalRecords = superMemberModel.controlPersonLegalModelRecords();
        if (CollectionUtils.isEmpty(controlPersonLegalRecords)) {
            return;
        }

        memberService.addControlPersonLegal(controlPersonLegalRecords);
        List<BasePersonInfo> basePersonInfos
                = AssistantUtils.mapListToList(controlPersonLegalRecords, BasePersonInfo.class);
        recordHandler.recordControlPersonLegal(basePersonInfos);
    }

    private void personOutControlStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> personOutControlRecords = superMemberModel.personOutControlModelRecords();
        if (CollectionUtils.isEmpty(personOutControlRecords)) {
            return;
        }

        memberService.addPersonOutControl(personOutControlRecords);
        List<PersonOutControlInfo> personOutControlInfos
                = AssistantUtils.mapListToList(personOutControlRecords, PersonOutControlInfo.class);
        recordHandler.recordPersonOutControl(personOutControlInfos);
    }

    private void majorPersonStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> majorPersonRecords = superMemberModel.majorPersonModelRecords();
        if (CollectionUtils.isEmpty(majorPersonRecords)) {
            return;
        }

        memberService.addMajorPerson(majorPersonRecords);
        List<MajorInvPersonInfo> majorInvPersonInfos
                = AssistantUtils.mapListToList(majorPersonRecords, MajorInvPersonInfo.class);
        recordHandler.recordMajorPerson(majorInvPersonInfos);
    }

    private void staffStorage(SuperMemberModel superMemberModel) {
        List<Map<String, Object>> staffRecords = superMemberModel.staffModelRecords();
        if (CollectionUtils.isEmpty(staffRecords)) {
            return;
        }

        memberService.addStaff(staffRecords);
        List<StaffPerson> staffPeople = AssistantUtils.mapListToList(staffRecords, StaffPerson.class);
        recordHandler.recordStaff(staffPeople);
    }

}
