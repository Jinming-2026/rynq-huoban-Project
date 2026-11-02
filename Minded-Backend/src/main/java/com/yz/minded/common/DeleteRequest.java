package com.yz.minded.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用删除请求体
 * @author yz
 */
@Data
public class DeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -7949595684895710760L;

    private long id;
}
