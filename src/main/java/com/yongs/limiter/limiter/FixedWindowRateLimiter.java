package com.yongs.limiter.limiter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;

/**
 * 功能：
 * 作者：YongS
 * 日期：2025/10/20 13:33
 */
public class FixedWindowRateLimiter {

    private final RedisTemplate<String,Object> redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:fixed:";

    private final DefaultRedisScript<Long> redisScript;

    /**
     * 初始化时，加载脚本文件
     */
    public FixedWindowRateLimiter(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>();
        //从类路径中加载lua脚本
        this.redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limit_fixed_window.lua")));
        this.redisScript.setResultType(Long.class);
    }

    /**
     * 固定滑动窗口
     * @param key 业务前缀
     * @param maxRequests 最大请求数
     * @param windowSeconds 窗口大小
     * @return
     */
    public boolean isAllowed(String key, int maxRequests,int windowSeconds){
        //当前窗口的序号，每个60秒为一个窗口
        long currentWindow = System.currentTimeMillis() / 1000 / windowSeconds;
        String redisKey = RATE_LIMIT_KEY_PREFIX + key + ":" + currentWindow;

        //执行脚本
        Long count = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(windowSeconds * 2)//过期时间2被窗口大小
        );

        return count != null && count < maxRequests;
    }
}
