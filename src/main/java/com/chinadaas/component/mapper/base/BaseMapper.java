package com.chinadaas.component.mapper.base;

import com.chinadaas.common.utils.BeanUtils;
import com.chinadaas.component.mapper.handler.AnnotationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author liubc
 * @version 1.0.0
 * @description neo4j查询结果映射模板
 * @createTime 2021.07.01
 */
@Slf4j
public abstract class BaseMapper implements Mapper {

    protected List<AnnotationHandler> handlers;

    /**
     * 根据泛型类型和neo4j结果获取相应的实体
     *
     * @param properties neo4j查询结果。如果为空，则该方法返回结果null
     * @param classType  要转换实体的Class类型。如果为空，则该方法返回结果null
     * @param <T>        要转换实体的泛型
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Map<String, Object> properties, Class<T> classType) {

        if (CollectionUtils.isEmpty(properties)
                || Objects.isNull(classType)) {

            return null;
        }

        return doGetBean(properties, classType);
    }

    /**
     * 子类实现具体构建实体逻辑
     *
     * @param properties neo4j查询结果。如果为空，则该方法返回结果null
     * @param classType  转换实体的Class类型。如果为空，则该方法返回结果null
     * @param <T>        要转换实体的泛型
     * @return
     */
    protected abstract <T> T doGetBean(Map<String, Object> properties, Class<T> classType);

    /**
     * orm处理
     *
     * @param properties neo4j查询结果。如果为空，则该方法返回结果null
     * @param classType  转换实体的Class类型。如果为空，则该方法返回结果null
     * @param <T>        要转换实体的泛型
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T> T propertyMappingBean(Map<String, Object> properties, Class<T> classType) {
        return BeanUtils.createBean(classType, properties, handlers);
    }
}
