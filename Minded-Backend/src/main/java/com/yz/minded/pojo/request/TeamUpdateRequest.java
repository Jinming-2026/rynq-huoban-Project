package com.yz.minded.pojo.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 修改队伍信息请求封装类
 * @author yz
 */
@Data
public class TeamUpdateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 2948773236074256882L;

    /**
     * 修改的队伍 id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date expireTime;

    /**
     * 0-公开、1-加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}
