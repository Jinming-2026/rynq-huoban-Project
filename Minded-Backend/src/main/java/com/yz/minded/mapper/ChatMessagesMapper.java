package com.yz.minded.mapper;

import com.yz.minded.pojo.ChatMessages;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author admin
* @description 针对表【chat_messages(用户私聊消息表)】的数据库操作Mapper
* @createDate 2024-10-29 18:58:02
* @Entity generator.domain.ChatMessages
*/
@Mapper
public interface ChatMessagesMapper extends BaseMapper<ChatMessages> {

}




