package com.chinadaas.service.impl;

import com.chinadaas.common.constant.MemberConst;
import com.chinadaas.common.constant.ModelStatus;
import com.chinadaas.entity.*;
import com.chinadaas.model.*;
import com.chinadaas.repository.MemberRepository;
import com.chinadaas.service.MemberService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.08.03
 */
@SuppressWarnings("unchecked")
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 子公司历史信息
     *
     * @param parentId
     * @return
     */
    @Override
    public List<MemberModel> obtainMembers(String parentId) {
        List<MemberEntity> entities = memberRepository.obtainMembers(parentId);
        List<MemberModel> models = Lists.newArrayList();

        for (MemberEntity entity : entities) {
            MemberModel model = new MemberModel().convertEntity2Model(entity);

            if (ModelStatus.HAVE_RESULT.equals(model.getResultStatus())) {
                models.add(model.calMemberResult());
            }
        }

        return models;
    }

    /**
     * 母公司及主要投资者个人共同控制企业
     *
     * @param parentId
     */
    @Override
    public List<DiscernAndMajorPersonModel> obtainDiscernAndMajorPersons(String parentId) {

        List<DiscernAndMajorPersonEntity> entities = memberRepository.obtainDiscernAndMajorPersons(parentId);
        List<DiscernAndMajorPersonModel> models = Lists.newArrayList();
        for (DiscernAndMajorPersonEntity entity : entities) {
            DiscernAndMajorPersonModel model = new DiscernAndMajorPersonModel(parentId).convertEntity2Model(entity);

            if (ModelStatus.HAVE_RESULT.equals(model.getResultStatus())) {
                models.add(model.calDiscernAndMajorPersonResult());
            }

        }

        return models;
    }

    /**
     * 母公司及其关键管理人员共同控制的公司
     *
     * @param parentId
     */
    @Override
    public List<DiscernAndStaffModel> obtainDiscernAndStaffs(String parentId) {

        List<DiscernAndStaffEntity> entities = memberRepository.obtainDiscernAndStaffs(parentId);
        List<DiscernAndStaffModel> models = Lists.newArrayList();
        for (DiscernAndStaffEntity entity : entities) {
            DiscernAndStaffModel model = new DiscernAndStaffModel(parentId).convertEntity2Model(entity);

            if (ModelStatus.HAVE_RESULT.equals(model.getResultStatus())) {
                models.add(model.calDiscernAndStaffResult());
            }

        }

        return models;
    }

    /**
     * 母公司法人对外直接控制的公司
     *
     * @param parentId
     */
    @Override
    public DiscernLegalOutModel obtainDiscernLegalOut(String parentId) {

        DiscernLegalOutEntity entity = memberRepository.obtainDiscernLegalOut(parentId);
        DiscernLegalOutModel model = new DiscernLegalOutModel(parentId).convertEntity2Model(entity);

        final BiFunction<String, String, List<Map>> commonPersonControlEnt
                = memberRepository::commonPersonControlEnt;

        return model.queryLegalControlEnts(commonPersonControlEnt);
    }

    /**
     * 母公司最终控股自然人任职法人企业
     *
     * @param parentId
     */
    @Override
    public ControlPersonLegalModel obtainControlPersonLegal(String parentId) {
        Map finalControlPerson = memberRepository.finalControlPerson(parentId);
        if (CollectionUtils.isEmpty(finalControlPerson)) {
            return new ControlPersonLegalModel(parentId);
        }

        Map finalControlProperty = (Map) finalControlPerson.get(MemberConst.FIN_CTRL_PROPERTY);
        String zsId = (String) finalControlProperty.get(MemberConst.ZSID);

        ControlPersonLegalEntity entity = memberRepository.obtainControlPersonLegal(zsId, parentId);
        ControlPersonLegalModel model =
                new ControlPersonLegalModel(parentId).converEntity2Model(entity, finalControlProperty);

        return model.processProperties();
    }

    /**
     * 母公司最终控股自然人对外控制的企业
     *
     * @param parentId
     */
    @Override
    public PersonOutControlModel obtainPersonOutControl(String parentId) {
        Map finalControlPerson = memberRepository.finalControlPerson(parentId);
        if (CollectionUtils.isEmpty(finalControlPerson)) {
            return new PersonOutControlModel(parentId, null);
        }

        Map finalControlProperty = (Map) finalControlPerson.get(MemberConst.FIN_CTRL_PROPERTY);
        String zsId = (String) finalControlProperty.get(MemberConst.ZSID);

        PersonOutControlEntity entity = memberRepository.obtainPersonOutControl(zsId, parentId);
        PersonOutControlModel model = new PersonOutControlModel(parentId, finalControlPerson).convertEntity2Model(entity);

        return model.processProperties();
    }

    /**
     * 母公司主要投资者个人直接对外控制公司
     *
     * @param parentId
     * @return
     */
    @Override
    public MajorPersonModel obtainMajorPerson(String parentId) {
        MajorPersonEntity majorPersonEntity = memberRepository.obtainMajorPerson(parentId);
        MajorPersonModel majorPersonModel = new MajorPersonModel(parentId).convertEntity2Model(majorPersonEntity);

        final BiFunction<String, String, List<Map>> commonPersonControlEnt
                = memberRepository::commonPersonControlEnt;

        return majorPersonModel.calMajorPersonModelResult(commonPersonControlEnt);
    }

    /**
     * 母公司关键管理人员直接对外控制公司
     *
     * @param parentId
     * @return
     */
    @Override
    public StaffModel obtainStaff(String parentId) {
        StaffEntity staffEntity = memberRepository.obtainStaff(parentId);
        StaffModel staffModel = new StaffModel(parentId).convertEntity2Model(staffEntity);

        final BiFunction<String, String, List<Map>> commonPersonControlEnt
                = memberRepository::commonPersonControlEnt;

        return staffModel.calStaffModelResult(commonPersonControlEnt);
    }

    @Override
    public void addMembers(List<Map<String, Object>> memberRecords) {
        memberRepository.addMembers(memberRecords);
    }

    @Override
    public void addDiscernAndMajorPerson(List<Map<String, Object>> discernAndMajorPersonRecords) {
        memberRepository.addDiscernAndMajorPerson(discernAndMajorPersonRecords);
    }
}
