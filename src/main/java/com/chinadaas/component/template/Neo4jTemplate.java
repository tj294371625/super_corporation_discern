package com.chinadaas.component.template;

import com.chinadaas.common.config.Neo4jConfiguration;
import com.chinadaas.commons.config.ThreadPoolConfig;
import com.chinadaas.commons.exception.QueryNeo4jTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.01
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class Neo4jTemplate {

    private final Neo4jConfiguration neo4jConfig;

    @Autowired
    public Neo4jTemplate(Neo4jConfiguration neo4jConfig) {
        this.neo4jConfig = neo4jConfig;
    }

    public List<Map<String, Object>> executeCypher(String cql, Map<String, Object> parameter) {
        Session session = neo4jConfig.getDriver().session();
        Transaction transaction = session.beginTransaction();
        StatementResult run = transaction.run(cql, parameter);
        ArrayList list = new ArrayList();

        while (run.hasNext()) {
            Record record = run.next();
            Map<String, Object> map = record.asMap();
            list.add(map);
        }

        transaction.success();
        transaction.close();
        return list;
    }

    public List<Map<String, Object>> executeCypher(String cql, Map<String, Object> parameter, long maxWaitTime) {
        Session session = neo4jConfig.getDriver().session();
        StatementResult result = session.run(cql, parameter);
        Future future = ThreadPoolConfig.QUERY_NEO4J_THREAD_POOL.submit(() -> {
            LinkedList recordList = new LinkedList();

            while (result.hasNext()) {
                recordList.add(result.next().asMap());
            }

            return recordList;
        });

        List resultList;
        try {
            resultList = (List) future.get(maxWaitTime, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException var13) {
            log.warn("查询异常，maxWaitTime:{}, cypher:{}, params:{}, msg:{}", new Object[]{maxWaitTime, cql, parameter, var13.getMessage()});
            future.cancel(true);
            throw new RuntimeException();
        } catch (TimeoutException var14) {
            log.warn("查询超时，maxWaitTime:{}, cypher:{}, params:{}", new Object[]{maxWaitTime, cql, parameter});
            future.cancel(true);
            session.close();
            throw new QueryNeo4jTimeOutException();
        } finally {
            if (session.isOpen()) {
                session.close();
            }

        }

        return resultList;
    }

}
