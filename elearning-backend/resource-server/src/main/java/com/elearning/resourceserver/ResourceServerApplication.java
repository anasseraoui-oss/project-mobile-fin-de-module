package com.elearning.resourceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResourceServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResourceServerApplication.class, args);
    }
}
