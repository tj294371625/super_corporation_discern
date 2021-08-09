package com.chinadaas.model;

import com.chinadaas.common.constant.ModelStatus;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 集团成员模型
 * @createTime 2021.08.03
 */
@SuppressWarnings("unchecked")
public class SuperMemberModel {

    private String currentQueryId;

    /**
     * 子公司
     */
    private List<MemberModel> memberModels;

    /**
     * 母公司及主要投资者个人共同控制企业
     */
    private List<DiscernAndMajorPersonModel> discernAndMajorPersonModels;

    /**
     * 母公司及其关键管理人员共同控制的公司
     */
    private List<DiscernAndStaffModel> discernAndStaffModels;

    /**
     * 母公司法人对外直接控制的公司
     */
    private DiscernLegalOutModel discernLegalOutModel;

    /**
     * 母公司最终控股自然人担任法人的企业
     */
    private ControlPersonLegalModel controlPersonLegalModel;

    /**
     * 母公司最终控股自然人对外控制的企业
     */
    private PersonOutControlModel personOutControlModel;

    /**
     * 母公司主要投资者个人直接对外控制公司
     */
    private MajorPersonModel majorPersonModel;

    /**
     * 母公司关键管理人员直接对外控制公司
     */
    private StaffModel staffModel;

    public SuperMemberModel(String currentQueryId) {
        this.currentQueryId = currentQueryId;
    }

    public List<Map<String, Object>> memberRecords() {
        if (CollectionUtils.isEmpty(memberModels)) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> results = Lists.newArrayList();
        for (MemberModel memberModel : memberModels) {
            if (ModelStatus.NO_RESULT.noEquals(memberModel.getResultStatus()))
                results.add(memberModel.getMergeResults());
        }

        return results;
    }

    public List<Map<String, Object>> discernAndMajorPersonRecords() {
        if (CollectionUtils.isEmpty(discernAndMajorPersonModels)) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> results = Lists.newArrayList();
        for (DiscernAndMajorPersonModel discernAndMajorPersonModel : discernAndMajorPersonModels) {
            if (ModelStatus.NO_RESULT.noEquals(discernAndMajorPersonModel.getResultStatus()))
                results.addAll(discernAndMajorPersonModel.getMergeResults());
        }

        return results;
    }

    public List<Map<String, Object>> discernAndStaffModelRecords() {
        if (CollectionUtils.isEmpty(discernAndStaffModels)) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> results = Lists.newArrayList();
        for (DiscernAndStaffModel discernAndStaffModel : discernAndStaffModels) {
            if (ModelStatus.NO_RESULT.noEquals(discernAndStaffModel.getResultStatus()))
                results.addAll(discernAndStaffModel.getMergeResults());
        }

        return results;
    }

    public List<Map<String, Object>> discernLegalOutModelRecords() {
        if (ModelStatus.NO_RESULT.noEquals(discernLegalOutModel.getResultStatus())) {
            return discernLegalOutModel.getLegalControlEnts();
        }

        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> controlPersonLegalModelRecords() {
        if (ModelStatus.NO_RESULT.noEquals(controlPersonLegalModel.getResultStatus())) {
            return controlPersonLegalModel.getResultList();
        }

        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> personOutControlModelRecords() {
        if (ModelStatus.NO_RESULT.noEquals(personOutControlModel.getResultStatus())) {
            return personOutControlModel.getResultList();
        }

        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> majorPersonModelRecords() {
        if (ModelStatus.NO_RESULT.noEquals(majorPersonModel.getResultStatus())) {
            return majorPersonModel.getMajorControlEnts();
        }

        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> staffModelRecords() {
        if (ModelStatus.NO_RESULT.noEquals(staffModel.getResultStatus())) {
            return staffModel.getStaffControlEnts();
        }

        return Collections.EMPTY_LIST;
    }

    public void setMemberModels(List<MemberModel> memberModels) {
        this.memberModels = memberModels;
    }

    public void setDiscernAndMajorPersonModels(List<DiscernAndMajorPersonModel> discernAndMajorPersonModels) {
        this.discernAndMajorPersonModels = discernAndMajorPersonModels;
    }

    public void setDiscernAndStaffModels(List<DiscernAndStaffModel> discernAndStaffModels) {
        this.discernAndStaffModels = discernAndStaffModels;
    }

    public void setDiscernLegalOutModel(DiscernLegalOutModel discernLegalOutModel) {
        this.discernLegalOutModel = discernLegalOutModel;
    }

    public void setControlPersonLegalModel(ControlPersonLegalModel controlPersonLegalModel) {
        this.controlPersonLegalModel = controlPersonLegalModel;
    }

    public void setPersonOutControlModel(PersonOutControlModel personOutControlModel) {
        this.personOutControlModel = personOutControlModel;
    }

    public void setMajorPersonModel(MajorPersonModel majorPersonModel) {
        this.majorPersonModel = majorPersonModel;
    }

    public void setStaffModel(StaffModel staffModel) {
        this.staffModel = staffModel;
    }

    public String getCurrentQueryId() {
        return currentQueryId;
    }
}
