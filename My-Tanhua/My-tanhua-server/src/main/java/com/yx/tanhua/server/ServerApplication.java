package com.yx.tanhua.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(
    exclude = {
        MongoAutoConfiguration.class, MongoDataAutoConfiguration.class
    },
    scanBasePackages = {
        "com.yx.tanhua.server", "com.yx.tanhua.common"
    })
@MapperScan("com.yx.tanhua.server.mapper")
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
