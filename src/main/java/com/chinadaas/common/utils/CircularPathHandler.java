package com.chinadaas.common.utils;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author liubc
 * @version 1.0.0
 * @description 环形路径检测器
 * @createTime 2021.07.01
 */
public class CircularPathHandler {

    /**
     * 快照列表
     */
    private final List<String> snapshotList;

    private CircularPathHandler() {
        this.snapshotList = Lists.newArrayList();
    }

    public static CircularPathHandler newInstance() {
        return new CircularPathHandler();
    }

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
     * 获取待更新链路的entId集合
     * 母公司本身不进行更新
     *
     * @param parentId
     * @return
     */
    public List<String> obtainChain(String parentId) {
        List<String> deepCopy = Lists.newArrayList(snapshotList.iterator());
        deepCopy.remove(parentId);
        return deepCopy;
    }
}
