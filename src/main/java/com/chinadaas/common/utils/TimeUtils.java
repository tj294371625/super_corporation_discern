package com.chinadaas.common.utils;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 计时工具
 * @createTime 2021.07.13
 */
public abstract class TimeUtils {

    public static long startTime() {
        return System.currentTimeMillis();
    }

    public static long endTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }
}
