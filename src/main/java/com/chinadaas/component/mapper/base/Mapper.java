package com.chinadaas.component.mapper.base;

import java.util.Map;

/**
 * @author liubc
 * @version 1.0.0
 * @description neo4j查询结果映射定义
 * @createTime 2021.07.01
 */
public interface Mapper {

    /*
     * neo4j的orm映射
     * */

    /**
     * 根据泛型类型和neo4j查询结果获取相应的实体
     *
     * @param properties neo4j查询结果。如果为空，则该方法返回结果null
     * @param classType  要转换实体的Class类型。如果为空，则该方法返回结果null
     * @param <T>        要转换实体的泛型
     * @return
     */
    <T> T getBean(Map<String, Object> properties, Class<T> classType);
}
