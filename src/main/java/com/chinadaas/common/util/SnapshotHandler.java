package com.chinadaas.common.util;

import com.chinadaas.common.constant.TargetType;
import com.chinadaas.component.wrapper.NodeWrapper;
import com.chinadaas.entity.ChainEntity;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 快照处理器
 * @createTime 2021.07.01
 */
public class SnapshotHandler {

    /**
     * 快照链表
     */
    private final List<String> snapshotList;

    /**
     * 链路总长度
     */
    private long totalChainLength;

    private SnapshotHandler() {
        this.snapshotList = Lists.newArrayList();
        this.totalChainLength = 0L;
    }

    public static SnapshotHandler newInstance() {
        return new SnapshotHandler();
    }

    /**
     * 链路长度累加
     *
     * @param partChainLength
     */
    public void chainLengthAccum(long partChainLength) {
        totalChainLength += partChainLength;
    }

    /**
     * 获取总链路长度
     *
     * @return
     */
    public long obtainTotalChainLength() {
        return totalChainLength;
    }

    /**
     * 环路检测
     *
     * @param entId
     * @return
     */
    public boolean circularCheck(String entId) {
        if (snapshotList.contains(entId)) {
            snapshotList.add(entId);
            return true;
        }

        snapshotList.add(entId);
        return false;
    }

    public ChainEntity resolveCircuit(List<NodeWrapper> circuitNodes) {

        // 按照注册资本分组
        Map<BigDecimal, List<NodeWrapper>> groupByRegCap = circuitNodes.stream()
                .collect(
                        Collectors.groupingBy(
                                NodeWrapper::obtainRegCap,
                                Collectors.toList()
                        )
                );
        // 返回注册资本最大的
        List<BigDecimal> regKeys = new ArrayList<>(groupByRegCap.keySet());
        Collections.sort(regKeys);
        Collections.reverse(regKeys);
        BigDecimal maxRegCapKey = regKeys.get(0);
        List<NodeWrapper> maxRegCaps = groupByRegCap.get(maxRegCapKey);
        if (maxRegCaps.size() <= 1) {
            return createPreChainEntity(maxRegCaps.get(0));
        }


        // 基于注册资本结果分组，按企业成立日期分组
        Map<Date, List<NodeWrapper>> groupByEsDate = maxRegCaps.stream()
                .collect(
                        Collectors.groupingBy(
                                NodeWrapper::obtainEsDate,
                                Collectors.toList()
                        )
                );
        // 返回注册资本最大的且注册日期最早的
        List<Date> dateKeys = new ArrayList<>(groupByEsDate.keySet());
        Collections.sort(dateKeys);
        Date earliestDate = dateKeys.get(0);
        List<NodeWrapper> earliestDates = groupByEsDate.get(earliestDate);
        if (earliestDates.size() <= 1) {
            return createPreChainEntity(earliestDates.get(0));
        }


        // 基于注册时间结果，按照拼音字典序排序
        List<NodeWrapper> pinYinList = earliestDates.stream()
                .sorted(Comparator.comparing(NodeWrapper::obtainNamePinYin))
                .collect(Collectors.toList());
        return createPreChainEntity(pinYinList.get(0));
    }

    private ChainEntity createPreChainEntity(NodeWrapper nodeWrapper) {
        ChainEntity preChainEntity = new ChainEntity();
        if (obtainNodeEntId().equals(nodeWrapper.obtainEntId())) {
            preChainEntity.setTempEntId("-1");
            preChainEntity.setTempName("");
            preChainEntity.setTargetType(TargetType.NON_EXIST.toString());
            return preChainEntity;
        }

        preChainEntity.setTempEntId(nodeWrapper.obtainEntId());
        preChainEntity.setTempName(nodeWrapper.obtainEntName());
        preChainEntity.setTargetType(TargetType.ENT.toString());

        return preChainEntity;
    }

    /**
     * 获取环状路径
     *
     * @return
     */
    public List<String> obtainCircularPath() {
        return snapshotList;
    }

    /**
     * 获取待更新点的entId
     *
     * @return
     */
    public String obtainNodeEntId() {
        return snapshotList.get(0);
    }

    public List<String> obtainCircularEntIds(String entId) {
        List<String> deepCopy = Lists.newArrayList(snapshotList.iterator());
        int index = deepCopy.indexOf(entId);
        return deepCopy.subList(index, deepCopy.size() - 1);
    }
}
