package com.yz.minded.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yz.minded.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author admin
* @description 针对表【user_center(用户中心表)】的数据库操作Mapper
* @createDate 2024-08-31 14:34:35
* @Entity generator.domain.UserCenter
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




