package com.chinadaas.entity.old;

import lombok.Data;

/*******************************************************************************
 * - Copyright (c)  2020  chinadaas.com
 * - File Name: BasePersonInfo
 * - @author: zhaolin - Initial implementation
 * - Description:
 *      人员节点输出基类
 * - Function List:
 *
 * - History:
 * Date         Author          Modification
 * 2020-01-19     zhaolin            Create the current class
 *******************************************************************************/
@Data
public class BasePersonInfo extends com.chinadaas.decision.model.out.BaseEntInfo {

    private String zspid;
    private String zsid;
    private String name;
    private String palgorithmid;
    private String person_country;
    private String person_country_desc;
    private String emid;
}