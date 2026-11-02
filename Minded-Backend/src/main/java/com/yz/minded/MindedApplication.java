package com.yz.minded;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = {"com.yz.minded.mapper"})
@EnableScheduling
public class MindedApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindedApplication.class, args);
    }

}
