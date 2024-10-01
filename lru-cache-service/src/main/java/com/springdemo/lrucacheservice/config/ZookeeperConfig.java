package com.springdemo.lrucacheservice.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {

    @Bean
    public CuratorFramework curatorFramework() {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("zookeeper:2181")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        return client;
    }
}