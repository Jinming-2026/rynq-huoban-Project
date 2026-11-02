package com.yz.minded.controller;

import com.yz.minded.common.BaseResponse;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.ResultUtils;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.dto.AddTagsDTO;
import com.yz.minded.pojo.dto.TagDTO;
import com.yz.minded.pojo.dto.UserTagsDTO;
import com.yz.minded.pojo.vo.ParentTagVo;
import com.yz.minded.pojo.vo.UserTagVo;
import com.yz.minded.service.UserService;
import com.yz.minded.service.UserTagService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 标签接口
 * @author yz
 */
@RestController
@RequestMapping("/tag")
//说明接口文件
@Api(value = "标签接口", tags = "标签管理相关的接口")
public class UserTagController {

    @Resource
    private UserService userService;

    @Resource
    private UserTagService userTagService;

    // 获取数据库中的所有标签
    @GetMapping("/all")
    public BaseResponse<List<UserTagVo>>  getAllTags(HttpServletRequest request) {
        // 获取用户的登录状态
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"用户未登录");
        }
        // 获取所有标签
        List<UserTagVo> userTagVoList = userTagService.getAllTags();
        return ResultUtils.success(userTagVoList);
    }

    /**
     * 用户更改标签
     * @param userTagsDTO   用户更改的标签内容
     * @return  更新信息
     */
    @PostMapping("/update")
    public BaseResponse<String> updateTags(@RequestBody UserTagsDTO userTagsDTO, HttpServletRequest request) {
        String tags = userTagsDTO.getTags();
        if (tags == null || !(tags.startsWith("[")&&tags.endsWith("]")) ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签格式错误");
        }
        User loginUser = userService.getLoginUser(request);
        String newTags = userTagService.updateTags(tags, loginUser);
        return ResultUtils.success(newTags);
    }

    // 获取所有父标签
    @GetMapping("/all/parent")
    public BaseResponse<List<ParentTagVo>> getAllParentTags() {
        List<ParentTagVo> allParentTags = userTagService.getAllParentTags();
        return ResultUtils.success(allParentTags);
    }

    /**
     * 用户创建标签
     * @param addTagsDTO    新标签
     * @return  创建信息
     */
    @PostMapping("/add")
    public BaseResponse<String> addTag(@RequestBody AddTagsDTO addTagsDTO, HttpServletRequest request) {
        if (addTagsDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String result = userTagService.addTag(addTagsDTO, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/del")
    public BaseResponse<Boolean> deleteTag(@RequestBody TagDTO tagDTO, HttpServletRequest request) {
        if (tagDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result = userTagService.deleteTag(tagDTO, loginUser);
        return ResultUtils.success(result);
    }
}
