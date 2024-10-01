package com.springdemo.lrucacheservice.service;

import com.springdemo.lrucacheservice.repository.LRUCache;
import com.springdemo.lrucacheservice.synchronizer.ZookeeperSynchronizer;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LRUCacheService {

    private final LRUCache cache;
    private final ZookeeperSynchronizer zookeeperSynchronizer;

    @Autowired
    public LRUCacheService(ZookeeperSynchronizer zookeeperSynchronizer) {
        this.cache = new LRUCache(10); // Cache capacity
        this.zookeeperSynchronizer = zookeeperSynchronizer;
    }

    public String getCacheEntry(String key) {
        return cache.get(key);
    }

    public void addCacheEntry(String key, String value) throws Exception {
        cache.put(key, value);

        // Sync the new cache entry to Zookeeper
        zookeeperSynchronizer.syncToZookeeper(key, value);
    }
}