package com.chinadaas.common.utils;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.LockSupport;

/**
 * @author liubc
 * @version 1.0.0
 * @description 线程工具
 * @createTime 2021.07.01
 */
public abstract class ThreadPoolUtils {

    public static void waitForLessThan(ThreadPoolExecutor poolExecutor, long lessThanTaskCount) {
        long taskCount = poolExecutor.getTaskCount();
        long completedTaskCount = poolExecutor.getCompletedTaskCount();
        while (taskCount - completedTaskCount > lessThanTaskCount) {
            LockSupport.parkNanos(1000 * 1000 * 500);
            taskCount = poolExecutor.getTaskCount();
            completedTaskCount = poolExecutor.getCompletedTaskCount();
        }
    }

    public static long getTaskSize(ThreadPoolExecutor poolExecutor) {
        long taskCount = poolExecutor.getTaskCount();
        long completedTaskCount = poolExecutor.getCompletedTaskCount();
        return taskCount - completedTaskCount;
    }

    public static void waitForComplete(ThreadPoolExecutor poolExecutor) {
        waitForLessThan(poolExecutor, 0);
    }

    public static void waitForCompleteAndShutdown(ThreadPoolExecutor poolExecutor) {
        waitForComplete(poolExecutor);
        poolExecutor.shutdown();
    }

}
