package com.segment.proxyservice.repository;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.segment.proxyservice.exceptions.KeyNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class CacheRepository {

    private LoadingCache<String, String> cache;

    public CacheRepository(LoadingCache<String, String> cache) {
        this.cache = cache;
    }

    public String get(String key) {
        try {
            return cache.getUnchecked(key);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            throw new KeyNotFoundException(String.format("Key=%s not found in the internal cache and external", key));
        }
    }
}
