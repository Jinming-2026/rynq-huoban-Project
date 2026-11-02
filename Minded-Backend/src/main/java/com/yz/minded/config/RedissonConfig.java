package com.yz.minded.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 * @author yz
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    private String password;

    @Bean
    public RedissonClient redissonClient() {
        // 1、创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s",host,port);     // 设置地址
        config.useSingleServer().setAddress(redisAddress).setDatabase(3).setPassword(password);
        // 2、创建 Redisson 实例
        // Sync and Async API
        return Redisson.create(config);
    }
}
