package com.springdemo.lrucacheservice.repository;


import java.util.HashMap;
import java.util.LinkedList;

public class LRUCache {

    private final int capacity;
    private final HashMap<String, String> cacheMap;
    private final LinkedList<String> orderList;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        this.orderList = new LinkedList<>();
    }

    // Get cache entry
    public String get(String key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }
        // Move the key to the end (most recently used)
        orderList.remove(key);
        orderList.addLast(key);
        return cacheMap.get(key);
    }

    // Add cache entry
    public void put(String key, String value) {
        if (cacheMap.containsKey(key)) {
            // Update the key's position in the list
            orderList.remove(key);
        } else if (cacheMap.size() == capacity) {
            // Evict the least recently used entry
            String leastRecentlyUsedKey = orderList.removeFirst();
            cacheMap.remove(leastRecentlyUsedKey);
        }
        // Add new entry to the cache and update order
        cacheMap.put(key, value);
        orderList.addLast(key);
    }

    // Check if the cache contains the key
    public boolean containsKey(String key) {
        return cacheMap.containsKey(key);
    }

    // Return cache size
    public int size() {
        return cacheMap.size();
    }

    // Clear the cache (if needed in synchronization logic)
    public void clear() {
        cacheMap.clear();
        orderList.clear();
    }
}