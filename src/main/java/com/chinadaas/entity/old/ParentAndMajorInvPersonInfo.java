package com.chinadaas.entity.old;

import lombok.Data;

/*******************************************************************************
 * - Copyright (c)  2020  chinadaas.com
 * - File Name: ParentAndMajorInvPersonInfo
 * - @author: zhaolin - Initial implementation
 * - Description:
 *      母公司以及主要投资者个人共同控制输出实体
 * - Function List:
 *
 * - History:
 * Date         Author          Modification
 * 2020-01-20     zhaolin            Create the current class
 *******************************************************************************/
@Data
public class ParentAndMajorInvPersonInfo extends MajorInvPersonInfo {

    private String conprop_person2sub;

    private String conprop_parent2sub;

    private String holderrto_person2sub;

    private String holderrto_parent2sub;


}