package com.yz.minded.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 子标签封装类
 * @author yz
 */
@Data
public class UserTagChildren implements Serializable {

    @Serial
    private static final long serialVersionUID = 753225196634025144L;
    /**
     * 子标签名称
     */
    private String text;

    /**
     * 子标签id
     */
    private String id;
}
