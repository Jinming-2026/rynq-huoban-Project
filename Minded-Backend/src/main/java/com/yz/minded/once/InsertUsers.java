package com.yz.minded.once;

import com.yz.minded.mapper.UserMapper;
import com.yz.minded.pojo.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;


@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    /**
     * 循环插入用户
     */
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("yusha");
            user.setAvatarUrl("https://yz-minded.oss-cn-chengdu.aliyuncs.com/userImg.png");
            user.setProfile("一条咸鱼");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("17711112222");
            user.setEmail("shayu-yusha@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println( stopWatch.getLastTaskTimeMillis());

    }
}
