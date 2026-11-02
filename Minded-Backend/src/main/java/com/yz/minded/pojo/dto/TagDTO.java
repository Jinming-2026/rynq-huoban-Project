package com.yz.minded.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TagDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7734065664604963916L;


    /**
     * 标签创建人 id
     */
    private Long userId;

    /**
     * 标签名
     */
    private String TagName;

}
