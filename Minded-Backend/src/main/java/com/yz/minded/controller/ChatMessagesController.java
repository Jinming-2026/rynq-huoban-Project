package com.yz.minded.controller;

import com.yz.minded.common.BaseResponse;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.ResultUtils;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.vo.ChatMessagesVo;
import com.yz.minded.service.ChatMessagesService;
import com.yz.minded.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/chat")
//说明接口文件
@Api(value = "用户聊天接口", tags = "用户聊天管理相关的接口")
public class ChatMessagesController {

    @Resource
    private UserService userService;

    @Resource
    private ChatMessagesService chatMessagesService;

    /**
     * 获取历史聊天消息
     * @param receiverId  接收者id
     * @param request   request
     * @return  历史聊天消息集合
     */
    @GetMapping("/history")
    public BaseResponse<List<ChatMessagesVo>> getHistoryMessages(@RequestParam("receiverId") long receiverId, HttpServletRequest request) {
        if (receiverId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long senderId = loginUser.getId();
        List<ChatMessagesVo> historyMessages = chatMessagesService.getHistoryMessages(senderId, receiverId);
        return ResultUtils.success(historyMessages);
    }
}
