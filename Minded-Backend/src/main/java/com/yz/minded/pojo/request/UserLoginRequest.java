package com.yz.minded.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6351047560044455370L;

    // 前端需要传入的参数
    private String userAccount;

    private String userPassword;

}
