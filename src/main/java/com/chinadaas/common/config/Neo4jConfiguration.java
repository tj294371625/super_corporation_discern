package com.chinadaas.common.config;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author lawliet
 * @version 1.0.0
 * @description neo4j连接配置
 * @createTime 2021.07.01
 */
@Slf4j
@Configuration
public class Neo4jConfiguration {

    @Value("${db.neo4j.user}")
    private String user;
    @Value("${db.neo4j.password}")
    private String password;
    @Value("${db.neo4j.url}")
    private String neo4jUrl;

    private Driver driver;


    @PostConstruct
    public void init() {
        log.info("creating neo4j driver, neo4j server url: [{}]", neo4jUrl);
        this.driver = GraphDatabase.driver(neo4jUrl, AuthTokens.basic(user, password));
    }

    public Driver getDriver() {
        return driver;
    }
}