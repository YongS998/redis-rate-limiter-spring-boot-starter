package com.yongs.limiter.limiter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;

/**
 * 功能：
 * 作者：YongS
 * 日期：2025/10/20 16:29
 */
public class LeakyBucketRateLimiter {

    private final RedisTemplate<String,Object> redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:leaky_bucket:";

    private final DefaultRedisScript<Long> redisScript;

    public LeakyBucketRateLimiter(RedisTemplate<String,Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/scripts/rate_limit_leaky_bucket.lua")));
        this.redisScript.setResultType(Long.class);
    }

    public boolean isAllowed(String key, int capacity, double leakRate){
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;

        long now = System.currentTimeMillis();

        Long count = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(capacity),
                String.valueOf(leakRate),
                String.valueOf(now)
        );

        return count != null && count == 1;
    }
}
