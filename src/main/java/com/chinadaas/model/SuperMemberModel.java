package com.chinadaas.model;

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
    private MemberModel memberModel;

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

}
