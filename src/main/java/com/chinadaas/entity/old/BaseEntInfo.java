package com.chinadaas.entity.old;

import com.chinadaas.commons.graph.model.GraphDto;
import lombok.Data;

import java.io.Serializable;

/*******************************************************************************
 * - Copyright (c)  2020  chinadaas.com
 * - File Name: BaseEntInfo
 * - @author: zhaolin - Initial implementation
 * - Description:
 *      企业对象输出基类
 * - Function List:
 *
 * - History:
 * Date         Author          Modification
 * 2020-01-19     zhaolin            Create the current class
 *******************************************************************************/
@Data
public class BaseEntInfo implements Serializable {
    private String parent_id;
    private String entid;
    private String entname;
    private String invtype;
    private String regno;
    private String creditcode;
    private String esdate;
    private String industryphy;
    private String regcap;
    private String entstatus;
    private String regcapcur;
    private String enttype;
    private String islist;
    private String code;
    private String stockcode;
    private String briefname;
    private String ent_country;
    private String type;
    private String industryco;
    private String province;
    private String usedname;
    private String enttype_desc;
    private String ent_country_desc;
    private String regcapcur_desc;
    private String industryphy_desc;
    private String industryco_desc;
    private String province_desc;
    private String break_law_count;
    private String punish_break_count;
    private String punished_count;
    private String abnormity_count;
    private String caseinfo_count;
    private String courtannoucement_count;
    private String gaccpenalty_count;
    private String judicial_aid_count;
    private String mab_info_count;
    private String finalcase_count;
    private String relation;
    private String relation_density;
    private String entstatus_desc;
    private GraphDto path;
    private String final_cgzb;

    public BaseEntInfo() {
    }
}