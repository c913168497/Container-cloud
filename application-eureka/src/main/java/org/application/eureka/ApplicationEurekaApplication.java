package org.application.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class ApplicationEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationEurekaApplication.class, args);
    }
}
