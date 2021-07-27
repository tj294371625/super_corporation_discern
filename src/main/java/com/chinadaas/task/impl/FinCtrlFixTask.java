package com.chinadaas.task.impl;

import com.chinadaas.task.FullTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author liubc
 * @version 1.0.0
 * @description
 * @createTime 2021.07.27
 */
@Slf4j
@Order(5)
@Component
public class FinCtrlFixTask implements FullTask {


    @Override
    public void run() {

    }
}
