package com.chinadaas.component.executor.impl;

import com.chinadaas.component.executor.Executor;
import com.chinadaas.component.io.EntIdListHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author liubc
 * @version 1.0.0
 * @description 单线程执行
 * @createTime 2021.07.03
 */
@Slf4j
@Primary
@Component("singleExecutor")
public class SingleExecutor implements Executor {

    private final EntIdListHolder entIdListHolder;

    @Autowired
    public SingleExecutor(EntIdListHolder entIdListHolder) {
        this.entIdListHolder = entIdListHolder;
    }

    @Override
    public void execute(String taskName, Consumer<String> consumer) {
        Set<String> entIdList = entIdListHolder.getEntIdList();

        AtomicLong counter = new AtomicLong(0L);
        for (String entId : entIdList) {

            if (0L == counter.addAndGet(1L) % 50_000L) {
                log.info("task: [{}] current read records: [{}] ", taskName, counter.get());
            }

            consumer.accept(entId);
        }
    }

}
