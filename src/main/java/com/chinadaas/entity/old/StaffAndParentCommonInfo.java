package com.chinadaas.entity.old;

import lombok.Data;

/*******************************************************************************
 * - Copyright (c)  2020  chinadaas.com
 * - File Name: StaffAndParentCommonInfo
 * - @author: zhaolin - Initial implementation
 * - Description:
 *      关键管理人员以及母公司共同控制实体信息
 * - Function List:
 *
 * - History:
 * Date         Author          Modification
 * 2020-01-20     zhaolin            Create the current class
 *******************************************************************************/
@Data
public class StaffAndParentCommonInfo extends StaffPerson {

    private String conprop_person2sub;

    private String conprop_parent2sub;


    private String holderrto_person2sub;
    private String holderrto_parent2sub;
}