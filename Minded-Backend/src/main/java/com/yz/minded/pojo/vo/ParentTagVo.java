package com.yz.minded.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 父标签返回封装实体类
 */
@Data
public class ParentTagVo implements Serializable {
    @Serial
    private static final long serialVersionUID = -3308653301553073352L;

    /**
     * 父标签名称
     */
    private String text;

    /**
     * 父标签id
     */
    private Long value;
}
