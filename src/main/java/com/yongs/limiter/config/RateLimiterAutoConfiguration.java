package com.yongs.limiter.config;

import com.yongs.limiter.annotation.RateLimit;
import com.yongs.limiter.aspect.RateLimitAspect;
import com.yongs.limiter.limiter.FixedWindowRateLimiter;
import com.yongs.limiter.limiter.LeakyBucketRateLimiter;
import com.yongs.limiter.limiter.SlidingWindowRateLimiter;
import com.yongs.limiter.limiter.TokenBucketRateLimiter;
import com.yongs.limiter.properties.RateLimiterProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 功能：
 * 作者：YongS
 * 日期：2025/10/20 20:48
 */
@AutoConfiguration //自动装配
@ConditionalOnClass({RedisTemplate.class, RateLimit.class})  //需要这个类
//注入这个Properties
@EnableConfigurationProperties(RateLimiterProperties.class)
//读取配置属性的enabled
@ConditionalOnProperty(prefix = "rate-limiter",name = "enabled",havingValue = "true",matchIfMissing = true)
@Import(RedisConfig.class)  //同时注入这个配置类
public class RateLimiterAutoConfiguration {

    /**
     * 固定窗口限流的Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public FixedWindowRateLimiter fixedWindowRateLimiter(RedisTemplate<String,Object> redisTemplate){
        return new FixedWindowRateLimiter(redisTemplate);
    }

    /**
     * 滑动窗口限流
     */
    @Bean
    @ConditionalOnMissingBean
    public SlidingWindowRateLimiter slidingWindowRateLimiter(RedisTemplate<String,Object> redisTemplate){
        return new SlidingWindowRateLimiter(redisTemplate);
    }

    /**
     * 漏桶限流
     */
    @Bean
    @ConditionalOnMissingBean
    public LeakyBucketRateLimiter leakyBucketRateLimiter(RedisTemplate<String,Object> redisTemplate){
        return new LeakyBucketRateLimiter(redisTemplate);
    }

    /**
     * 令牌桶
     */
    @Bean
    @ConditionalOnMissingBean    public TokenBucketRateLimiter tokenBucketRateLimiter(RedisTemplate<String,Object> redisTemplate){
        return new TokenBucketRateLimiter(redisTemplate);
    }

    /**
     * 限流AOP的Bean
     */    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(FixedWindowRateLimiter fixedWindowRateLimiter,
                                           SlidingWindowRateLimiter slidingWindowRateLimiter,
                                           LeakyBucketRateLimiter leakyBucketRateLimiter,
                                           TokenBucketRateLimiter tokenBucketRateLimiter){
        return new RateLimitAspect(
                fixedWindowRateLimiter,
                slidingWindowRateLimiter,
                leakyBucketRateLimiter,
                tokenBucketRateLimiter
        );
    }
}
