package com.chinadaas.component.io;

import com.chinadaas.common.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Objects;
import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 企业名单加载器
 * @createTime 2021.07.01
 */
@Slf4j
@Component
public class EntIdListLoader {

    /**
     * 加载器状态
     * false表示未占用
     * true表示占用
     */
    private volatile boolean status;

    private final EntIdListHolder entIdListHolder;

    @Autowired
    public EntIdListLoader(EntIdListHolder entIdListHolder) {
        this.status = false;
        this.entIdListHolder = entIdListHolder;
    }

    public boolean lock() {

        // double check
        if (BooleanUtils.isFalse(status)) {

            synchronized (this) {
                if (BooleanUtils.isFalse(status)) {
                    this.status = true;
                    return true;
                }
                return false;
            }

        }

        return false;
    }

    public synchronized void unlock() {

        if (BooleanUtils.isTrue(status)) {
            this.status = false;
            return;
        }

        throw new IllegalStateException("illegal lock status");
    }

    public void loadEntIdList(String resourcePath) {
        log.info("start load entId list...");
        long startTime = System.currentTimeMillis();
        try {
            File dir = new File(resourcePath);
            Assert.isTrue(dir.isDirectory(), "must input a directory path");
            File[] sourceFiles = dir.listFiles();
            Assert.nonNull(sourceFiles, "directory must not empty");

            for (File sourceFile : sourceFiles) {
                doLoadEntIdList(sourceFile);
            }

            log.info(
                    "end the load entId list, list size: [{}], spend time: [{}ms]",
                    entIdListHolder.size(),
                    System.currentTimeMillis() - startTime
            );

        } catch (IOException e) {
            log.error("load resource fail, reason: ", e);
            throw new RuntimeException();
        }
    }

    /**
     * 重新加载企业名单
     *
     * @param entIds
     * @return
     */
    public void reloadEntIdList(Set<String> entIds) {

        entIdListHolder.clear();

        for (String entId : entIds) {
            entIdListHolder.add(entId);
        }

    }

    private void doLoadEntIdList(File resourceFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(resourceFile));

        String line;
        while (Objects.nonNull(line = br.readLine())) {
            if (StringUtils.isNotBlank(line)) {
                entIdListHolder.add(line);
            }
        }

    }

}
