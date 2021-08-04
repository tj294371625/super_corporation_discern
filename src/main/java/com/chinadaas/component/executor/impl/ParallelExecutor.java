package com.chinadaas.component.executor.impl;

import com.chinadaas.common.util.ThreadPoolUtils;
import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.io.EntIdListHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 并行执行
 * @createTime 2021.07.01
 */
@Slf4j
@Component("parallelExecutor")
public class ParallelExecutor implements Executor {

    private final ThreadPoolExecutor executor;
    private final EntIdListHolder entIdListHolder;

    @Autowired
    public ParallelExecutor(ThreadPoolExecutor executor,
                            EntIdListHolder entIdListHolder) {

        this.executor = executor;
        this.entIdListHolder = entIdListHolder;
    }

    @Override
    public void execute(String taskName, Consumer<String> consumer) {
        Set<String> entIdList = entIdListHolder.getEntIdList();

        final long THRESHOLD = 100_000L;
        AtomicLong counter = new AtomicLong(0L);
        for (String entId : entIdList) {

            if (0L == counter.addAndGet(1L) % 50_000L) {
                log.info("task: [{}] current read records: [{}] ", taskName, counter.get());
            }

            ThreadPoolUtils.waitForLessThan(executor, THRESHOLD);
            // task execute
            executor.submit(() -> consumer.accept(entId));
        }

        // wait task execute complete
        ThreadPoolUtils.waitForComplete(executor);
    }

}
