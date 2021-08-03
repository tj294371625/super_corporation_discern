package com.chinadaas.component.wrapper;

import com.chinadaas.common.utils.AssistantUtils;
import com.chinadaas.commons.graph.model.NodeDto;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 点
 * @createTime 2021.07.01
 */
@Slf4j
public class NodeWrapper extends NodeDto {

    public String obtainZsId() {
        Map<String, Object> properties = getProperties();
        return (String) properties.get("zsid");
    }

    public String obtainEmc() {
        Map<String, Object> properties = getProperties();
        return (String) properties.get("emc");
    }

    public String obtainEmId() {
        return (String) getProperties().get("emid");
    }

    /**
     * 当前决策权点企业entId
     *
     * @return
     */
    public String obtainEntId() {
        Map<String, Object> properties = getProperties();
        return (String) properties.get("nodeid");
    }


    public String obtainEntName() {
        return (String) getProperties().get("name");
    }

    public String obtainCreditCode() {
        return (String) getProperties().get("creditcode");
    }

    public String obtainRegNo() {
        return (String) getProperties().get("regno");
    }

    public String obtainEntstatus() {
        return (String) getProperties().get("entstatus");
    }

    public String obtainNodeType() {
        Map<String, Object> properties = getProperties();
        String invType = (String) properties.get("invtype");
        return AssistantUtils.getNodeType(invType);
    }

    /**
     * 当前决策权点成立日期
     *
     * @return
     */
    public Date obtainEsDate() {
        Map<String, Object> properties = getProperties();
        String esDateStr = (String) properties.get("esdate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date esDate = null;
        try {
            esDate = sdf.parse(esDateStr);
        } catch (ParseException e) {
            log.error("日期格式错误: [{}]", esDateStr);
        }
        return esDate;
    }

    /**
     * 当前决策权点注册资本
     *
     * @return
     */
    public BigDecimal obtainRegCap() {
        Map<String, Object> properties = getProperties();
        Object regcap = properties.get("regcap");
        return new BigDecimal(Objects.isNull(regcap) ? "0.000000" : regcap.toString());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
