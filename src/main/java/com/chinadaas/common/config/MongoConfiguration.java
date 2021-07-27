package com.chinadaas.common.config;

import com.mongodb.MongoClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * @author lawliet
 * @version 1.0.0
 * @description mongodb配置
 * @createTime 2021.07.05
 */
@Slf4j
@Configuration
public class MongoConfiguration {

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDbFactory mongoDbFactory,
                                                       MongoMappingContext mongoMappingContext) {

        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        // remove _class field
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

    @Bean
    public MongoClientOptions mongoOptions() {
        return MongoClientOptions.builder().socketTimeout(60_000).build();
    }

}