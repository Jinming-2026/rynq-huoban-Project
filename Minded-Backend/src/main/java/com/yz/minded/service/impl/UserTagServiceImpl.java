package com.yz.minded.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.exception.BusinessException;
import com.yz.minded.pojo.User;
import com.yz.minded.pojo.UserTag;
import com.yz.minded.pojo.dto.AddTagsDTO;
import com.yz.minded.pojo.dto.TagDTO;
import com.yz.minded.pojo.vo.ParentTagVo;
import com.yz.minded.pojo.vo.UserTagChildren;
import com.yz.minded.pojo.vo.UserTagVo;
import com.yz.minded.service.UserService;
import com.yz.minded.service.UserTagService;
import com.yz.minded.mapper.UserTagMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
* @author admin
* @description 针对表【user_tag(标签表)】的数据库操作Service实现
* @createDate 2024-10-08 19:45:24
*/
@Service
public class UserTagServiceImpl extends ServiceImpl<UserTagMapper, UserTag>
    implements UserTagService{

    @Resource
    private UserService userService;

    @Resource
    private UserTagMapper userTagMapper;

    /**
     * 获取所有标签
     * @return  构造好的父子标签树
     */
    @Override
    public List<UserTagVo> getAllTags() {
        List<UserTagVo> result = new ArrayList<>(); // 需要返回的实体类集合结果
        List<UserTag> tagList = this.list();    // 获取所有的标签
        // 构造父子标签树
        tagList.stream()
                .filter(tagParent -> tagParent.getIsParent() == 0)  // 子对父标签进行遍历
                .forEach(item -> {
                    // 父标签
                    UserTagVo userTagVo = new UserTagVo();
                    userTagVo.setText(item.getTagName());   // 存下父标签
                    long parentId = item.getId();   // 父标签id

                    List<UserTagChildren> userTagChildrenList = new ArrayList<>();  // 子标签集合
                    tagList.stream()
                            // 根据父标签过滤属于该层父标签下的子标签
                            .filter(tagChildren -> tagChildren.getIsParent() == 1 && tagChildren.getParentId() == parentId)
                            .forEach(childrenItem -> {
                                UserTagChildren userTagChildren = new UserTagChildren();
                                userTagChildren.setId(childrenItem.getTagName());    // 子标签id
                                userTagChildren.setText(childrenItem.getTagName()); //子标签名称
                                userTagChildrenList.add(userTagChildren);
                            });
                    // 将子标签装入所属父标签实体类中
                    userTagVo.setChildren(userTagChildrenList);
                    // 将所包装好的父子标签装入返回的实体类集合
                    result.add(userTagVo);
                });
        return result;
    }

    /**
     * 更新用户标签
     * @param tags  用户更改的标签
     * @param loginUser 登录用户
     * @return  更改信息
     */
    @Override
    public String updateTags(String tags, User loginUser) {
//        1. 效验标签的格式，只接受String类型的格式标签，参数效验
        if (tags == null || !(tags.startsWith("[") && tags.endsWith("]"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签格式错误");
        }
        System.out.println(tags);
        User newUser = new User();
        BeanUtils.copyProperties(loginUser, newUser);
        newUser.setTags(tags);
//        2. 使用 gson 将标签格式化
        Gson gson = new Gson();     // 创建一个 gson 对象，用于执行 JSON 和 Java 对象之间的相互转换
/*       TypeToken 是 Gson 提供的一个工具类，用来表示复杂泛型类型的数据结构。
         因为 Java 的泛型在运行时会丢失具体的类型信息，所以需要使用 TypeToken 来获取包含泛型信息的 Type 对象。*/
        Type listType = new TypeToken<List<String>>() {
        }.getType();    // 生成一个表示 List<String> 类型的 Type 对象。
        // 将 JSON 字符串解析为 Java 对象
        List<String> tagList = gson.fromJson(newUser.getTags(), listType);
//        3. 用户最多选择10个标签
        if (tagList.size() > 10 && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多选择 10 个标签");
        }
//        4. 更新用户数据
        boolean result = userService.updateById(newUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return tags;
    }

    /**
     * 获取所有父标签
     * @return  所有父标签
     */
    @Override
    public List<ParentTagVo> getAllParentTags() {
        List<UserTag> allTagList = this.list(); // 所有标签集合
        // 获取所有父标签
        return allTagList.stream()
                .filter(item -> item.getIsParent() == 0)    // 过滤，只需要父标签
                .map(t -> {
                    // 构造父标签
                    ParentTagVo parentTagVo = new ParentTagVo();
                    parentTagVo.setText(t.getTagName());
                    parentTagVo.setValue(t.getId());
                    return parentTagVo;
                }).toList();
    }

    /**
     * 创建标签
     * @param addTagsDTO    创建标签信息
     * @param loginUser     登录用户
     * @return  创建标签信息
     */
    @Override
    public String addTag(AddTagsDTO addTagsDTO, User loginUser) {
        // 全部标签转换成小写用于判断是否和数据库中的标签重复
        // 参数效验
        if (addTagsDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 效验是否重复创建标签
        String newChildrenTagName = addTagsDTO.getTagName().toLowerCase();  // 转成小写
        String newParentTagName = null;
        if (addTagsDTO.getParentName() != null) {   // 防止空指针异常
            newParentTagName = addTagsDTO.getParentName().toLowerCase();
        }
        // 判断标签是否重复
        // 获取所有标签
        List<UserTagVo> allTags = this.getAllTags();
        for (UserTagVo tag: allTags) {
            if (newParentTagName != null) { // 创建父标签
                // 判断需要创建的父标签是否重复
                if (newParentTagName.equals(tag.getText().toLowerCase())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"父标签已存在");
                }
            }
            // 判断创建的子标签有没有重复
            for (UserTagChildren childrenTag: tag.getChildren()) {  // 遍历子标签
                if (newChildrenTagName.equals(childrenTag.getText().toLowerCase())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"子标签已存在");
                }
            }
        }
        // 创建标签、设置标签参数
        UserTag userTag = new UserTag();
        userTag.setUserId(loginUser.getId());   // 设置标签创建人
        if (newParentTagName == null) {
            // 只创建子标签
            userTag.setIsParent(1); // 设置标识为非父标签（子标签）
            userTag.setTagName(addTagsDTO.getTagName());    // 设置标签名
            BeanUtils.copyProperties(addTagsDTO, userTag);
            int insertResult = userTagMapper.insert(userTag);
            if (insertResult == 1) {
                return "添加成功";
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建子标签失败");
            }
        }
        // 添加父子标签
        String parentTagName = addTagsDTO.getParentName();
        userTag.setTagName(parentTagName); // 设置父标签名
        userTag.setIsParent(0);     // 设置标识为父标签
        // 添加父标签
        int  result = userTagMapper.insert(userTag);
        if (result == 1) {  // 父标签创建成功
            userTag = new UserTag();
            // 根据创建的父标签查询其 id
            QueryWrapper<UserTag> userTagQueryWrapper = new QueryWrapper<>();
            userTagQueryWrapper.eq("tagName", parentTagName);
            List<UserTag> userTags = this.list(userTagQueryWrapper);
            if (userTags == null || userTags.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建父标签失败");
            }
            Long parentTagId = userTags.get(0).getId();
            // 创建表标签
            userTag.setIsParent(1);
            userTag.setParentId(parentTagId);
            userTag.setTagName(addTagsDTO.getTagName());
            userTag.setUserId(loginUser.getId());
            int insert = userTagMapper.insert(userTag);
            if (insert != 1) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建子标签失败");
            }
        }
        return "添加成功";
    }

    /**
     * 删除标签（逻辑删除）
     * @param tagDTO    标签名，创建人id
     * @param loginUser 登录用户
     */
    @Override
    public Boolean deleteTag(TagDTO tagDTO, User loginUser) {
        // 效验参数
        if (tagDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        String tagName = tagDTO.getTagName();   // 标签名
        // 判断该标签是否存在
        QueryWrapper<UserTag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("tagName", tagName);
        Long result = userTagMapper.selectCount(tagQueryWrapper);
        if (result <=0) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"没有该标签");
        }
        // 权限效验 --- 管理员可删除任何标签 自己只能删除自己创建的标签
        boolean isAdmin = userService.isAdmin(loginUser);   // true - 管理员
        UserTag userTag = userTagMapper.selectOne(tagQueryWrapper);
        if (!isAdmin && loginUser.getId() != userTag.getUserId()) {
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        // 若删除的是父标签，则父标签下的子标签也要被同时删除
        if (userTag.getIsParent() == 1) {    // 父标签 - 0 ，子标签 - 1
            // 删除子标签
            int deletedById = userTagMapper.deleteById(userTag.getId());
            if (deletedById <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
            }
        }
        // 删除父标签，及其父标签下的所有子标签
        tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("parentId", userTag.getId());
        int deleteChildTag = userTagMapper.delete(tagQueryWrapper);
        int deletedById = userTagMapper.deleteById(userTag.getId());
        if (deletedById <= 0 && deleteChildTag <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }

        return true;
    }
}




