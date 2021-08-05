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
public class SuperMemberModel {

    /**
     * 子公司
     */
    private List<MemberModel> memberModels;

    /**
     * 母公司及主要投资者个人共同控制企业
     */
    private DiscernAndMajorPersonModel discernAndMajorPersonModel;

    /**
     * 母公司及其关键管理人员共同控制的公司
     */
    private DiscernAndStaffModel discernAndStaffModel;

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

    public SuperMemberModel(List<MemberModel> memberModels,
                            DiscernAndMajorPersonModel discernAndMajorPersonModel,
                            DiscernAndStaffModel discernAndStaffModel,
                            DiscernLegalOutModel discernLegalOutModel,
                            ControlPersonLegalModel controlPersonLegalModel,
                            PersonOutControlModel personOutControlModel,
                            MajorPersonModel majorPersonModel,
                            StaffModel staffModel) {

        this.memberModels = memberModels;
        this.discernAndMajorPersonModel = discernAndMajorPersonModel;
        this.discernAndStaffModel = discernAndStaffModel;
        this.discernLegalOutModel = discernLegalOutModel;
        this.controlPersonLegalModel = controlPersonLegalModel;
        this.personOutControlModel = personOutControlModel;
        this.majorPersonModel = majorPersonModel;
        this.staffModel = staffModel;
    }

    public List<Map<String, Object>> memberRecords() {
        if (CollectionUtils.isEmpty(memberModels)) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> results = Lists.newArrayList();
        for (MemberModel memberModel : memberModels) {
            if (!ModelStatus.NO_RESULT.equals(memberModel.getResultStatus()))
                results.add(memberModel.getMergeResults());
        }

        return results;
    }

    public List<Map<String, Object>> discernAndMajorPersonRecords() {

        return discernAndMajorPersonModel.getMergeResults();
    }
}
