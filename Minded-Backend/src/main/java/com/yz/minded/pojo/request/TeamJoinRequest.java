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
 * 用户加入队伍请求封装类
 * @author yz
 */
@Data
public class TeamJoinRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3173400685051245891L;
    /**
     * 加入的队伍 id
     */
    private Long teamId;

    /**
     * 加密队伍的密码
     */
    private String password;
}
