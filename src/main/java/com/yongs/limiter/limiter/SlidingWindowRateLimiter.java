package com.yongs.limiter.limiter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;

/**
 * 功能：
 * 作者：YongS
 * 日期：2025/10/20 14:11
 */
public class SlidingWindowRateLimiter {

    private final RedisTemplate<String,Object> redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:sliding:";

    private final DefaultRedisScript<Long> redisScript;

    public SlidingWindowRateLimiter(RedisTemplate<String,Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>();
        //加载lua文件
        this.redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/scripts/rate_limit_slide_window.lua")));
        this.redisScript.setResultType(Long.class);
    }

    /**
     * 滑动窗口限流
     * @param key
     * @param maxRequests
     * @param windowSeconds
     * @return
     */
    public boolean isAllowed(String key,int maxRequests,int windowSeconds){
        long now = System.currentTimeMillis();//当前时间戳
        long windowStart = now - windowSeconds * 1000L;//窗口起始时间戳

        String redisKey = RATE_LIMIT_KEY_PREFIX + key;

        //执行lua脚本
        Long count = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(now),//当前时间戳
                String.valueOf(windowStart),//窗口起始时间戳
                String.valueOf(maxRequests),//最大请求数
                String.valueOf(windowSeconds)//窗口大小
        );

        return count != null && count == 1L;
    }
}
