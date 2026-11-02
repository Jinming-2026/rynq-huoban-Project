package com.yz.minded.pojo.dto;

import com.yz.minded.pojo.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 队伍查询封装类
 * 为什么要设置请求参数包装类：
 * 1、请求参数名称和实体类不一样
 * 2、有一些参数用不到，如果要生成接口文档，会增加理解成本
 * 3、多个实体类映射到同一个对象
 * @author yz
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;


    /**
     * 队伍描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 0-公开、1-加密
     */
    private Integer status;

    /**
     * 是否过期 true -查询过期队伍    false - 不查询
     * 默认查询未过期队伍
     */
    private Boolean timeOut = false;

}
