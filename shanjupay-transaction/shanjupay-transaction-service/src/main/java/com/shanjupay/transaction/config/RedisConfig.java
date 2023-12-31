package com.shanjupay.transaction.config;

import com.shanjupay.common.cache.Cache;
import com.shanjupay.transaction.common.util.RedisCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * redis配置类
 */

@Configuration
public class RedisConfig {
    @Bean
    public Cache cache(StringRedisTemplate stringRedisTemplate) {
        return new RedisCache(stringRedisTemplate);
    }
}
