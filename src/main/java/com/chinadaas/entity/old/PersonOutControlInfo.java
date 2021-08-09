package com.chinadaas.entity.old;

import com.chinadaas.commons.graph.model.GraphDto;
import lombok.Data;

/*******************************************************************************
 * - Copyright (c)  2020  chinadaas.com
 * - File Name: PersonOutControlInfo
 * - @author: zhaolin - Initial implementation
 * - Description:
 *      母公司控股自然人对外控制输出实体
 * - Function List:
 *
 * - History:
 * Date         Author          Modification
 * 2020-01-20     zhaolin            Create the current class
 *******************************************************************************/
@Data
public class PersonOutControlInfo extends BasePersonInfo {

    private GraphDto ctrl2parent_path;
    private GraphDto ctrl2source_path;

    private String ctrl2parent_cgzb;
    private String ctrl2source_cgzb;
}