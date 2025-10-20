package com.yongs.limiter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 功能：
 * 作者：YongS
 * 日期：2025/10/20 20:51
 */
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
