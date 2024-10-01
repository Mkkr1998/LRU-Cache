package com.springdemo.lrucacheservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LruCacheServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LruCacheServiceApplication.class);
    }
}
