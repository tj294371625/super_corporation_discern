package com.chinadaas.component.executor;

import java.util.Set;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 执行器
 * @createTime 2021.07.01
 */
public interface Executor {

    /**
     * 任务执行
     *
     * @param taskName 要执行的任务名称
     * @param consumer 要执行的任务
     */
    void execute(String taskName, Consumer<String> consumer);

    void executeTasks(String taskName, Consumer<Set<String>> consumer);

}
