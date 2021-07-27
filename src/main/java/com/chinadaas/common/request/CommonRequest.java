package com.chinadaas.common.request;

import com.chinadaas.common.constant.ImportMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author lawliet
 * @version 1.0.0
 * @description 通用请求
 * @createTime 2021.07.20
 */
@Setter
@Getter
@ToString
public class CommonRequest implements Serializable {

    ImportMode mode;

}
