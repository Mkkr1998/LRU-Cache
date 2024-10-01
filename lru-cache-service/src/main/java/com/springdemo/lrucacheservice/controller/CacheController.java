package com.springdemo.lrucacheservice.controller;

import com.springdemo.lrucacheservice.repository.LRUCache;
import com.springdemo.lrucacheservice.service.LRUCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cache")
public class CacheController {

    private final LRUCacheService cacheService;


    public CacheController(LRUCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/{key}")
    public String getCache(@PathVariable String key) {
        return cacheService.getCacheEntry(key);
    }

    @PostMapping
    public String addCache(@RequestParam String key, @RequestParam String value) throws Exception {
        cacheService.addCacheEntry(key, value);
        return "Cache updated";
    }
}