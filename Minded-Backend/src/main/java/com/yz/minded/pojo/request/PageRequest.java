package com.yz.minded.pojo.request;

import lombok.Data;

/**
 * 通用分页请求参数
 * @author yz
 */
@Data
public class PageRequest {

    /**
     * 当前是第几页
     */
    protected int pageNum = 1;

    /**
     * 页面大小
     */
    protected int pageSize = 8;

}
