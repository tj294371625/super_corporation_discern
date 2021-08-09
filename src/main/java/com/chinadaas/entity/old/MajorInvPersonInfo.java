package com.chinadaas.entity.old;

import lombok.Data;

/*******************************************************************************
 * - Copyright (c)  2020  chinadaas.com
 * - File Name: MajorInvPersonInfo
 * - @author: zhaolin - Initial implementation
 * - Description:
 *      主要投资者个人输出实体
 * - Function List:
 *
 * - History:
 * Date         Author          Modification
 * 2020-01-20     zhaolin            Create the current class
 *******************************************************************************/
@Data
public class MajorInvPersonInfo extends BasePersonInfo{

    private String conprop_person2parent;

    private String holderrto_person2parent;

}