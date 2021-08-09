package com.chinadaas.common.util;

import com.chinadaas.entity.SuperCorporationEntity;
import com.chinadaas.entity.old.BaseEntInfo;
import com.chinadaas.entity.old.ParentAndMajorInvPersonInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 记录处理
 * @createTime 2021.07.03
 */
@Slf4j
@Component
public class RecordHandler {

    private static final Object LOCK = new Object();

    /**
     * 记录新增或者更新类型的增量名单
     */
    private final Set<String> auTypeIncrSet;

    /**
     * 记录删除类型的增量名单
     */
    private final Set<String> delTypeIncrSet;

    public RecordHandler() {
        this.auTypeIncrSet = Collections.newSetFromMap(new ConcurrentHashMap<>(200_000));
        this.delTypeIncrSet = Collections.newSetFromMap(new ConcurrentHashMap<>(200_000));
    }

    public void recordAUTypeIncr(Collection<String> entIds) {
        auTypeIncrSet.addAll(entIds);
    }

    public Set<String> auTypeIncrSet() {
        return this.auTypeIncrSet;
    }

    public void clearAUTypeIncr() {
        auTypeIncrSet.clear();
    }

    public void recordDelTypeIncr(Collection<String> entIds) {
        this.delTypeIncrSet.addAll(entIds);
    }

    public Set<String> delTypeIncrSet() {
        return this.delTypeIncrSet;
    }

    public void clearDelTypeIncr() {
        this.delTypeIncrSet.clear();
    }


    @Value("${data.path.members}")
    private String memberDataPath;
    @Value("${data.filename.members}")
    private String memberDataFileName;
    @Value("${data.filename.discernAndMajorPerson}")
    private String discernAndMajorPersonDataFileName;
    @Value("${data.filename.discernAndStaff}")
    private String discernAndStaffDataFileName;
    @Value("${data.filename.discernLegalOut}")
    private String discernLegalOutDataFileName;
    @Value("${data.filename.controlPersonLegal}")
    private String controlPersonLegalDataFileName;
    @Value("${data.filename.personOutControl}")
    private String personOutControlDataFileName;
    @Value("${data.filename.majorPerson}")
    private String majorPersonDataFileName;
    @Value("${data.filename.staff}")
    private String staffDataFileName;

    @Value("${data.path.super}")
    private String superDataPath;
    @Value("${data.filename.super}")
    private String superDataFileName;
    @Value("${data.path.incr}")
    private String incrListPath;
    @Value("${data.filename.incr}")
    private String incrListFileName;
    @Value("${data.path.timeout}")
    private String timeoutPath;
    @Value("${data.filename.timeout}")
    private String timeoutFileName;
    @Value("${data.path.circular}")
    private String circularPath;
    @Value("${data.filename.circular}")
    private String circularFileName;

    public void recordMembers(List<BaseEntInfo> baseEntInfos) {
        List<String> memberRecords = AssistantUtils.memberRecord(baseEntInfos);

        if (CollectionUtils.isEmpty(memberRecords)) {
            return;
        }

        String tempName = Thread.currentThread().getName() + "-" + memberDataFileName;

        doRecord(memberRecords, memberDataPath, tempName);
    }

    public void recordDiscernAndMajorPerson(List<ParentAndMajorInvPersonInfo> parentAndMajorInvPersonInfos) {
        AssistantUtils.discernAndMajorPersonRecord(parentAndMajorInvPersonInfos);
    }

    public void recordDiscernAndStaff() {

    }

    public void recordDiscernLegalOut() {

    }

    public void recordControlPersonLegal() {

    }

    public void recordPersonOutControl() {

    }

    public void recordMajorPerson() {

    }

    public void recordStaff() {

    }

    public void recordSuperCorporation(SuperCorporationEntity superCorporationEntity) {
        String superCorporationStr = AssistantUtils.superCorporationRecord(superCorporationEntity);

        if (StringUtils.isBlank(superCorporationStr)) {
            return;
        }

        String tempName = Thread.currentThread().getName() + "-" + superDataFileName;
        doRecord(superCorporationStr, superDataPath, tempName);

    }

    /**
     * 记录增量名单
     *
     * @param entId
     */
    public void recordIncrList(String entId) {
        if (StringUtils.isBlank(incrListPath)
                || StringUtils.isBlank(incrListFileName)
                || StringUtils.isBlank(entId)) {
            return;
        }

        doRecord(entId, incrListPath, incrListFileName);
    }

    /**
     * 记录查询超时的entId
     *
     * @param entId
     */
    public void recordTimeOut(String entId) {

        if (StringUtils.isBlank(timeoutPath)
                || StringUtils.isBlank(timeoutFileName)
                || StringUtils.isBlank(entId)) {
            return;
        }

        doRecord(entId, timeoutPath, timeoutFileName);
    }

    /**
     * 记录环路
     *
     * @param circularSnapshot
     */
    public void recordCircular(List<String> circularSnapshot) {

        if (StringUtils.isBlank(circularPath)
                || StringUtils.isBlank(circularFileName)
                || CollectionUtils.isEmpty(circularSnapshot)) {
            return;
        }

        String joinStr = String.join(",", circularSnapshot);
        doRecord(joinStr, circularPath, circularFileName);
    }

    private void doRecord(String message, String path, String fileName) {
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File outputFile = new File(path + File.separator + fileName);

        try (
                FileOutputStream fos = new FileOutputStream(outputFile, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
        ) {
            bw.write(message + "\r\n");
            bw.flush();
        } catch (IOException e) {
            log.warn("write message: [{}] fail.", message);
        }
    }

    private void doRecord(List<String> messages, String path, String fileName) {
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File outputFile = new File(path + File.separator + fileName);

        try (
                FileOutputStream fos = new FileOutputStream(outputFile, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
        ) {
            for (String message : messages) {
                bw.write(message + "\r\n");
                bw.flush();
            }
        } catch (IOException e) {
            log.warn("write messages fail.");
        }
    }
}
