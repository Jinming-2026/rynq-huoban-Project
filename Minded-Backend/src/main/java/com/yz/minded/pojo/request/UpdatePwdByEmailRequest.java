package com.yz.minded.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UpdatePwdByEmailRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6137785866504989860L;

    private String userAccount;

    private String userPassword;

    private String email;

    private String emailCode;
}
