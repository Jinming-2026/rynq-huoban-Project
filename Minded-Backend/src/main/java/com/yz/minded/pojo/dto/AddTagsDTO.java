package com.yz.minded.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建标签请求体
 * @author yz
 */
@Data
public class AddTagsDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3850452274767881805L;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 父标签名称
     */
    private String parentName;

    /**
     * 父标签 id
     */
    private Long parentId;
}
