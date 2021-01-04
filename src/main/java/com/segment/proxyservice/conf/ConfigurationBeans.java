package com.segment.proxyservice.conf;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAutoConfiguration
public class ConfigurationBeans {
    private static Logger log = LoggerFactory.getLogger(ConfigurationBeans.class);

    @Value("${redis.hostname}")
    private String hostname;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.expiry}")
    private int expiry;

    @Value("${redis.capacity}")
    private int capacity;

    @Bean
    public JedisPool jedisPool() {
        log.info("Starting JedisPool with hostname={}, port={}", hostname, port);

        return new JedisPool(hostname, port);
    }

    @Bean
    public LoadingCache<String, String> loadingCache() {
        log.info("Starting LoadingCache with capacity={}, expiry={}", capacity, expiry);

        return CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, TimeUnit.SECONDS)
                .maximumSize(capacity)
                .build(new CacheLoader<String, String>() {
                    public String load(String key) {
                        try (Jedis jedis = jedisPool().getResource()) {
                            return jedis.get(key);
                        }
                    }
                });
    }
}
