package com.chinadaas.service;

import com.chinadaas.model.*;

import java.util.List;
import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.08.03
 */
public interface MemberService {

    /**
     * 子公司
     *
     * @param parentId
     * @return
     */
    List<MemberModel> obtainMembers(String parentId);

    /**
     * 母公司及主要投资者个人共同控制企业
     *
     * @param parentId
     */
    List<DiscernAndMajorPersonModel> obtainDiscernAndMajorPersons(String parentId);

    /**
     * 母公司及其关键管理人员共同控制的公司
     *
     * @param parentId
     */
    List<DiscernAndStaffModel> obtainDiscernAndStaffs(String parentId);

    /**
     * 母公司法人对外直接控制的公司
     *
     * @param parentId
     */
    DiscernLegalOutModel obtainDiscernLegalOut(String parentId);

    /**
     * 母公司最终控股自然人任职法人企业
     *
     * @param parentId
     */
    ControlPersonLegalModel obtainControlPersonLegal(String parentId);

    /**
     * 母公司最终控股自然人对外控制的企业
     *
     * @param parentId
     */
    PersonOutControlModel obtainPersonOutControl(String parentId);

    /**
     * 母公司主要投资者个人直接对外控制公司
     *
     * @param parentId
     * @return
     */
    MajorPersonModel obtainMajorPerson(String parentId);

    /**
     * 母公司关键管理人员直接对外控制公司
     *
     * @param parentId
     * @return
     */
    StaffModel obtainStaff(String parentId);
    
    void addMembers(List<Map<String, Object>> memberRecords);

    void addDiscernAndMajorPerson(List<Map<String, Object>> discernAndMajorPersonRecords);

    void addDiscernAndStaff(List<Map<String, Object>> discernAndStaffRecords);

    void addDiscernLegalOut(List<Map<String, Object>> discernLegalOutRecords);

    void addControlPersonLegal(List<Map<String, Object>> controlPersonLegalRecords);

    void addPersonOutControl(List<Map<String, Object>> personOutControlRecords);

    void addMajorPerson(List<Map<String, Object>> majorPersonRecords);

    void addStaff(List<Map<String, Object>> staffRecords);
}
