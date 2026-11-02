package com.yz.minded.pojo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求体
 * @author yz
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest implements Serializable {

    @Serial     // 指定类的序列化版本号
    private static final long serialVersionUID = 5672681499837294833L;

    // 前端需要传入的参数
    private String username;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String email;
}
