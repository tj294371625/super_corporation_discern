package com.chinadaas.component.mapper;

import com.chinadaas.component.mapper.base.BaseMapper;
import com.chinadaas.component.mapper.handler.AnnotationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 通用映射
 * @createTime 2021.07.01
 */
@Primary
@Component
public class CommonMapper extends BaseMapper {

    @Autowired
    public CommonMapper(List<AnnotationHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T doGetBean(Map<String, Object> properties, Class<T> classType) {
        return propertyMappingBean(properties, classType);
    }
}
