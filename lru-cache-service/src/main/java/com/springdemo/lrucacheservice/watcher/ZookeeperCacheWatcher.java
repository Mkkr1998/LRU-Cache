package com.springdemo.lrucacheservice.watcher;

import com.springdemo.lrucacheservice.service.LRUCacheService;
import com.springdemo.lrucacheservice.synchronizer.ZookeeperSynchronizer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperCacheWatcher {

    private static final String ZK_CACHE_PATH = "/cache";
    private final CuratorFramework zookeeperClient;
    private final LRUCacheService cacheService;
    private final ZookeeperSynchronizer zookeeperSynchronizer;

    public ZookeeperCacheWatcher(CuratorFramework zookeeperClient, LRUCacheService cacheService, ZookeeperSynchronizer zookeeperSynchronizer) {
        this.zookeeperClient = zookeeperClient;
        this.cacheService = cacheService;
        this.zookeeperSynchronizer = zookeeperSynchronizer;

        // Start watching Zookeeper for cache changes
        try {
            PathChildrenCache cacheWatcher = new PathChildrenCache(zookeeperClient, ZK_CACHE_PATH, true);
            cacheWatcher.getListenable().addListener(new CacheSyncListener());
            cacheWatcher.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CacheSyncListener implements PathChildrenCacheListener {

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                String path = event.getData().getPath();
                String key = path.substring(ZK_CACHE_PATH.length() + 1);
                String value = new String(event.getData().getData());

                // Sync the new key-value pair to the local cache
                cacheService.addCacheEntry(key, value);

                // Increment the sync counter
                zookeeperSynchronizer.incrementSyncCounter(key);

                // Check if all instances have synchronized
                if (zookeeperSynchronizer.hasAllInstancesSynced(key)) {
                    // If all instances have synced, remove the entry from Zookeeper
                    zookeeperSynchronizer.clearCacheEntry(key);
                }
            }
        }
    }
}