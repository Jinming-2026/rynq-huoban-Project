package com.yz.minded.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class UserTagVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 460165660747755107L;

    /**
     * 父标签
     */
    private String text;

    /**
     * 子标签
     */
    private List<UserTagChildren> children;
}
