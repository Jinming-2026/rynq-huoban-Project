package com.yz.minded.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yz.minded.pojo.User;
import com.yz.minded.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 * @author yz
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;    // 引入 Redis

    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    List<Long> mainUserList = List.of(1L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 59 23 * * ? ")  // cron 表达式
    public void doCacheRecommendUser() {
        // 获取锁的对象
        RLock lock = redissonClient.getLock("minded:precachejob:docache:lock");
        // 写锁（判断是否抢到锁）
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                for (Long userId: mainUserList) {
                    // 无缓存，则进行查数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("minded:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 对查出的数据，进行写缓存
                    try {
                        // timeout: 30000 - 30s （redis 过期时间）
                        valueOperations.set(redisKey,userPage,60000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("Redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error: { e }", e);
        } finally {
            // 一定要放入 finally
            // 判断当前的锁是不是当前线程所产生的锁（只能释放自己的锁）（每个线程都有不唯一的线程 id）
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();  // 释放锁
            }
        }


    }
}
