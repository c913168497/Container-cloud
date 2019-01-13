package com.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient // 开启服务发现
@EnableFeignClients // 开启声明式的web service客户端
public class ApplicationDockerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationDockerApplication.class, args);
    }

}

