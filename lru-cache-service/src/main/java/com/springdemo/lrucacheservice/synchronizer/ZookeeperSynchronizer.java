package com.springdemo.lrucacheservice.synchronizer;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ZookeeperSynchronizer {

    private static final String ZK_CACHE_PATH = "/cache";
    private static final String ZK_SERVICES_PATH = "/services";
    private static final String ZK_SYNC_COUNTER_PATH = "/cache_sync_counter";

    private final CuratorFramework zookeeperClient;
    private final PathChildrenCache servicesCache;

    public ZookeeperSynchronizer(CuratorFramework zookeeperClient) throws Exception {
        this.zookeeperClient = zookeeperClient;

        // Initialize a PathChildrenCache to monitor the changes under /services (ephemeral nodes for each service instance)
        this.servicesCache = new PathChildrenCache(zookeeperClient, ZK_SERVICES_PATH, true);
        servicesCache.start(); // Start listening for node changes
    }

    public void syncToZookeeper(String key, String value) throws Exception {
        String zkPath = ZK_CACHE_PATH + "/" + key;
        String syncCounterPath = getSyncCounterPath(key);

        // Check if cache entry exists in Zookeeper, if not, create it
        if (zookeeperClient.checkExists().forPath(zkPath) == null) {
            // Create new cache entry in Zookeeper
            zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath, value.getBytes());
        } else {
            // Update existing cache entry
            zookeeperClient.setData().forPath(zkPath, value.getBytes());
        }

        // Ensure the sync counter path exists before incrementing the sync counter
        if (zookeeperClient.checkExists().forPath(syncCounterPath) == null) {
            zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(syncCounterPath, "0".getBytes());
        }
    }

    private String getSyncCounterPath(String key) {
        return ZK_SYNC_COUNTER_PATH + "/" + key;
    }

    public void incrementSyncCounter(String key) throws Exception {
        String syncCounterPath = getSyncCounterPath(key);

        // Ensure node existence before accessing
        if (zookeeperClient.checkExists().forPath(syncCounterPath) == null) {
            // Log the creation of the sync counter node
            System.out.println("Creating sync counter node: " + syncCounterPath);
            zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(syncCounterPath, "0".getBytes());
        }

        byte[] data = zookeeperClient.getData().forPath(syncCounterPath);
        int counter = Integer.parseInt(new String(data));
        counter++;

        // Log the new counter value
        System.out.println("Updating sync counter for " + syncCounterPath + " to " + counter);

        // Update sync counter in Zookeeper
        zookeeperClient.setData().forPath(syncCounterPath, String.valueOf(counter).getBytes());
    }

    public boolean hasAllInstancesSynced(String key) throws Exception {
        String syncCounterPath = getSyncCounterPath(key);
        byte[] data = zookeeperClient.getData().forPath(syncCounterPath);
        int counter = Integer.parseInt(new String(data));

        // Dynamically get the number of live instances
        int liveInstancesCount = getLiveInstanceCount();

        // Check if all instances have synced
        return counter >= liveInstancesCount;
    }

    public void clearCacheEntry(String key) throws Exception {
        String zkPath = ZK_CACHE_PATH + "/" + key;
        String syncCounterPath = getSyncCounterPath(key);

        // Delete cache entry and sync counter from Zookeeper
        zookeeperClient.delete().forPath(zkPath);
        zookeeperClient.delete().forPath(syncCounterPath);
    }

    // Method to dynamically get the number of live instances
    public int getLiveInstanceCount() throws Exception {
        // Fetch the list of child nodes (representing instances) under /services
        List<String> instances = zookeeperClient.getChildren().forPath(ZK_SERVICES_PATH);
        return instances.size(); // Return the count of live instances
    }

    // Register the service as an ephemeral node in Zookeeper
    public void registerInstance(String instanceId) throws Exception {
        String instancePath = ZK_SERVICES_PATH + "/" + instanceId;

        // Create an ephemeral node for the service instance
        zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(instancePath);

        // Set a watch on the /services path to detect when instances are added or removed
        servicesCache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                case CHILD_REMOVED:
                    System.out.println("Instance added/removed: " + event.getData().getPath());
                    break;
                default:
                    break;
            }
        });
    }
}