package com.chinadaas.repository;

import com.chinadaas.entity.*;

import java.util.List;
import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description TODO
 * @createTime 2021.08.03
 */
public interface MemberRepository {
    /**
     * 子公司历史信息
     *
     * @param parentId
     */
    List<MemberEntity> obtainMembers(String parentId);

    /**
     * 母公司及主要投资者个人共同控制企业
     *
     * @param parentId
     */
    List<DiscernAndMajorPersonEntity> obtainDiscernAndMajorPersons(String parentId);

    /**
     * 母公司及其关键管理人员共同控制的公司
     *
     * @param parentId
     */
    List<DiscernAndStaffEntity> obtainDiscernAndStaffs(String parentId);

    /**
     * 母公司法人对外直接控制的公司
     *
     * @param parentId
     */
    DiscernLegalOutEntity obtainDiscernLegalOut(String parentId);

    /**
     * 母公司最终控股自然人任职法人企业
     *
     * @param zsId
     * @param parentId
     */
    ControlPersonLegalEntity obtainControlPersonLegal(String zsId, String parentId);

    /**
     * 母公司最终控股自然人对外控制的企业
     *
     * @param zsId
     * @param parentId
     */
    PersonOutControlEntity obtainPersonOutControl(String zsId, String parentId);

    /**
     * 母公司主要投资者个人直接对外控制公司
     *
     * @param parentId
     * @return
     */
    MajorPersonEntity obtainMajorPerson(String parentId);

    /**
     * 母公司关键管理人员直接对外控制公司
     *
     * @param parentId
     * @return
     */
    StaffEntity obtainStaff(String parentId);

    /**
     * 自然人控股企业通用查询
     *
     * @param zsId
     * @param parentId
     * @return
     */
    List<Map> commonPersonControlEnt(String zsId, String parentId);

    /**
     * 获取最终控股自然人zsId
     *
     * @param parentId
     * @return
     */
    Map finalControlPerson(String parentId);
}
