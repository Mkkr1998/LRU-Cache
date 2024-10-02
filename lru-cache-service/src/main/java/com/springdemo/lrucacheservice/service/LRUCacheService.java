package com.springdemo.lrucacheservice.service;

import com.springdemo.lrucacheservice.repository.LRUCache;
import com.springdemo.lrucacheservice.synchronizer.ZookeeperSynchronizer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LRUCacheService {

    private final LRUCache cache;
    private final ZookeeperSynchronizer zookeeperSynchronizer;
    private final String instanceId;

    @Autowired
    public LRUCacheService(ZookeeperSynchronizer zookeeperSynchronizer) {
        this.cache = new LRUCache(100); // Cache capacity
        this.zookeeperSynchronizer = zookeeperSynchronizer;
        this.instanceId = UUID.randomUUID().toString(); // Generate a unique ID for the instance
    }

    @PostConstruct
    public void registerInstance() {
        try {
            // Register the instance with Zookeeper as an ephemeral node
            zookeeperSynchronizer.registerInstance(instanceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
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