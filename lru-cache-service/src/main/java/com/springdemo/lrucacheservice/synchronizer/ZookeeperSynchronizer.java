package com.springdemo.lrucacheservice.synchronizer;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperSynchronizer {

    private static final String ZK_CACHE_PATH = "/cache";
    private static final String ZK_SYNC_COUNTER_PATH = "/cache_sync_counter";
    private final CuratorFramework zookeeperClient;
    private final int numberOfInstances = 3; // Adjust based on the number of instances


    public ZookeeperSynchronizer(CuratorFramework zookeeperClient) {
        this.zookeeperClient = zookeeperClient;
    }

    public void syncToZookeeper(String key, String value) throws Exception {
        String zkPath = ZK_CACHE_PATH + "/" + key;
        String syncCounterPath = getSyncCounterPath(key);

        if (zookeeperClient.checkExists().forPath(zkPath) == null) {
            // Create new cache entry in Zookeeper and initialize sync counter
            zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath, value.getBytes());
            zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(syncCounterPath, "0".getBytes());
        } else {
            // Update existing cache entry
            zookeeperClient.setData().forPath(zkPath, value.getBytes());
        }
    }

    private String getSyncCounterPath(String key) {
        return ZK_SYNC_COUNTER_PATH + "/" + key;
    }

    public void incrementSyncCounter(String key) throws Exception {
        String syncCounterPath = getSyncCounterPath(key);
        byte[] data = zookeeperClient.getData().forPath(syncCounterPath);
        int counter = Integer.parseInt(new String(data));
        counter++;

        // Update the sync counter in Zookeeper
        zookeeperClient.setData().forPath(syncCounterPath, String.valueOf(counter).getBytes());
    }

    public boolean hasAllInstancesSynced(String key) throws Exception {
        String syncCounterPath = getSyncCounterPath(key);
        byte[] data = zookeeperClient.getData().forPath(syncCounterPath);
        int counter = Integer.parseInt(new String(data));

        // Check if all instances have synced
        return counter >= numberOfInstances;
    }

    public void clearCacheEntry(String key) throws Exception {
        String zkPath = ZK_CACHE_PATH + "/" + key;
        String syncCounterPath = getSyncCounterPath(key);

        // Delete cache entry and sync counter from Zookeeper
        zookeeperClient.delete().forPath(zkPath);
        zookeeperClient.delete().forPath(syncCounterPath);
    }
}