package com.chinadaas.common.util;

import com.google.common.collect.Lists;

import java.util.List;

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

    /**
     * 获取待更新链路的entId集合
     * 母公司本身不进行更新
     *
     * @param parentId
     * @return
     */
    public List<String> obtainChainEntIds(String parentId) {
        List<String> deepCopy = Lists.newArrayList(snapshotList.iterator());
        deepCopy.remove(parentId);
        return deepCopy;
    }
}
