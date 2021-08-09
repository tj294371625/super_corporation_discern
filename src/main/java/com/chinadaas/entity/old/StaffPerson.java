package com.chinadaas.entity.old;

import lombok.Data;

/*******************************************************************************
 * - Copyright (c)  2020  chinadaas.com
 * - File Name: StaffPerson
 * - @author: zhaolin - Initial implementation
 * - Description:
 *      关键管理人员相关输出
 * - Function List:
 *
 * - History:
 * Date         Author          Modification
 * 2020-01-20     zhaolin            Create the current class
 *******************************************************************************/
@Data
public class StaffPerson extends BasePersonInfo {
    private String position;
    private String position_desc;


}