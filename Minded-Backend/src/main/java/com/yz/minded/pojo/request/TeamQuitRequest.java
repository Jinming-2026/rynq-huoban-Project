package com.yz.minded.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 退出队伍请求封装类
 * @author yz
 */
@Data
public class TeamQuitRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -614486942172563636L;

    /**
     * 退出的队伍 id
     */
    private Long teamId;

}
