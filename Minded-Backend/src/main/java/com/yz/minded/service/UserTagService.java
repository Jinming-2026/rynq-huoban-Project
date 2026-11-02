package com.yz.minded.service;

import com.yz.minded.pojo.User;
import com.yz.minded.pojo.UserTag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yz.minded.pojo.dto.AddTagsDTO;
import com.yz.minded.pojo.dto.TagDTO;
import com.yz.minded.pojo.vo.ParentTagVo;
import com.yz.minded.pojo.vo.UserTagVo;

import java.util.List;

/**
* @author admin
* @description 针对表【user_tag(标签表)】的数据库操作Service
* @createDate 2024-10-08 19:45:24
*/
public interface UserTagService extends IService<UserTag> {

    /**
     * 查询所有标签
     * @return  所有标签构成的父子标签树
     */
    List<UserTagVo> getAllTags();

    /**
     * 更新用户标签
     * @param tags  用户更改的标签
     * @param loginUser 登录用户
     * @return  更改信息
     */
    String updateTags(String tags, User loginUser);

    /**
     * 获取所有的父标签
     * @return  所有父标签
     */
    List<ParentTagVo> getAllParentTags();

    /**
     * 创建标签
     * @param addTagsDTO    创建标签信息
     * @param loginUser     登录用户
     * @return  创建标签信息
     */
    String addTag(AddTagsDTO addTagsDTO, User loginUser);

    /**
     * 删除标签
     * @param tagDTO    标签名，创建人id
     * @param loginUser 登录用户
     * @return  del
     */
    Boolean deleteTag(TagDTO tagDTO,User loginUser);
}
