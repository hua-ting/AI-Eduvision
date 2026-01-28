package com.learning.recommend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 学习推荐系统启动类
 */
@SpringBootApplication
@MapperScan("com.learning.recommend.mapper")
public class RecommendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("学习推荐系统启动成功!");
        System.out.println("接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("========================================\n");
    }
}
