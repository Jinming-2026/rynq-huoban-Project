package com.yz.minded.common;

import lombok.Getter;

/**
 * 错误码
 * @author yz
 */
@Getter
public enum ErrorCode {

    SUCCESS(200,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"数据为空",""),
    NOT_LOGIN(40100,"用户未登录",""),
    NO_AUTH(40101,"用户无权限",""),
    SYSTEM_ERROR(50000,"系统内部错误","");

    // 状态码
    private final int code;
    // 错误码信息
    private final String message;
    // 错误码详细信息
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

}
