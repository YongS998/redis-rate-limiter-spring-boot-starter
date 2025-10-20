package com.yongs.limiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流key
     */
    String key() default "";

    /**
     * 时间窗口
     */
    int window() default 60;

    /**
     * 最大请求数
     */
    int maxRequests() default 100;

    /**
     * 限流类型
     */
    LimitType type() default LimitType.FIXED_WINDOW;

    /**
     * 桶容量
     */
    int capacity() default 100;

    /**
     * 速率
     */
    int rate() default 10;

    /**
     * 所需令牌数
     */
    int tokens() default 1;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后尝试";

    enum LimitType{
        FIXED_WINDOW,//固定窗口
        SLIDING_WINDOW,//滑动窗口
        TOKEN_BUCKET,//令牌桶
        LEAKY_BUCKET;//漏桶
    }
}
