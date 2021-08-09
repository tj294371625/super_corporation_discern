package com.chinadaas.component.io;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 企业名单持有类
 * @createTime 2021.07.01
 */
@Slf4j
@Component
public class EntIdListHolder {

    private Set<String> entIdList;

    public EntIdListHolder() {
        this.entIdList = Sets.newHashSet();
    }

    public boolean add(String entId) {
        return entIdList.add(entId);
    }

    public void clear() {
        entIdList.clear();
    }

    public Set<String> getEntIdList() {
        return entIdList;
    }

    /**
     * 获取分割的名单
     *
     * @return
     */
    public List<List<String>> getDivideEntIdList() {
        int partitionSize = Runtime.getRuntime().availableProcessors() * 2;

        log.info("将企业标识名单分割: [{}]份", partitionSize);

        Iterator<String> iterator = entIdList.iterator();
        List<String> entIdListCopy = Lists.newArrayList(iterator);
        return Lists.partition(entIdListCopy, partitionSize);
    }
}
