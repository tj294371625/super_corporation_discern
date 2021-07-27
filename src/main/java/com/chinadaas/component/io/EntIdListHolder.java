package com.chinadaas.component.io;


import com.google.common.collect.Sets;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 企业名单持有类
 * @createTime 2021.07.01
 */
@Getter
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
}
