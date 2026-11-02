package com.yz.minded.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改密码请求参数
 * @author yz
 */
@Data
public class UpdatePwdRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 9001821702257909109L;

    /**
     * 用户密码
     */
    private String oldPassword;

    /**
     * 需要修改成的密码
     */
    private String newPassword;

    /**
     * 效验修改的密码
     */
    private String checkPassword;
}
