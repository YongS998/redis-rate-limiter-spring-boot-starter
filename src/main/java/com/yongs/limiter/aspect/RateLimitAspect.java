package com.yongs.limiter.aspect;

import com.yongs.limiter.annotation.RateLimit;
import com.yongs.limiter.limiter.FixedWindowRateLimiter;
import com.yongs.limiter.limiter.LeakyBucketRateLimiter;
import com.yongs.limiter.limiter.SlidingWindowRateLimiter;
import com.yongs.limiter.limiter.TokenBucketRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;


/**
 * 功能：
 * 作者：YongS
 * 日期：2025/10/20 17:15
 */
@Aspect
@Component
public class RateLimitAspect {

    private final FixedWindowRateLimiter fixedWindowRateLimiter;
    private final SlidingWindowRateLimiter slidingWindowRateLimiter;
    private final LeakyBucketRateLimiter leakyBucketRateLimiter;
    private final TokenBucketRateLimiter tokenBucketRateLimiter;

    public RateLimitAspect(FixedWindowRateLimiter fixedWindowRateLimiter,
                           SlidingWindowRateLimiter slidingWindowRateLimiter,
                           LeakyBucketRateLimiter leakyBucketRateLimiter,
                           TokenBucketRateLimiter tokenBucketRateLimiter) {
        this.fixedWindowRateLimiter = fixedWindowRateLimiter;
        this.slidingWindowRateLimiter = slidingWindowRateLimiter;
        this.leakyBucketRateLimiter = leakyBucketRateLimiter;
        this.tokenBucketRateLimiter = tokenBucketRateLimiter;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint,rateLimit);
        boolean allowed;

        switch (rateLimit.type()){
            case FIXED_WINDOW -> allowed = fixedWindowRateLimiter.isAllowed(key, rateLimit.maxRequests(), rateLimit.window());
            case SLIDING_WINDOW -> allowed = slidingWindowRateLimiter.isAllowed(key, rateLimit.maxRequests(), rateLimit.window());
            case LEAKY_BUCKET -> allowed = leakyBucketRateLimiter.isAllowed(key, rateLimit.capacity(), rateLimit.rate());
            case TOKEN_BUCKET -> allowed = tokenBucketRateLimiter.isAllowed(key,rateLimit.capacity(),rateLimit.rate(),rateLimit.tokens());
            default -> allowed = fixedWindowRateLimiter.isAllowed(key, rateLimit.maxRequests(), rateLimit.window());
        }

        if (!allowed){
            throw new RuntimeException(rateLimit.message());
        }

        return joinPoint.proceed();
    }

    /**
     * 生成限流key
     * @param joinPoint
     * @param rateLimit
     * @return
     */
    private String generateKey(ProceedingJoinPoint joinPoint,RateLimit rateLimit){
        String key = rateLimit.key();
        if (StringUtils.hasText(key)){
            return key;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();

        //获取方法参数
        Object[] args = joinPoint.getArgs();
        StringBuilder keyBuilder = new StringBuilder(className).append(":").append(methodName);

        //如果有HttpServletRequest参数，添加IP地址
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest){
                String ip = getClientIP((HttpServletRequest) arg);
                keyBuilder.append(":").append(ip);
                break;
            }
        }

        return keyBuilder.toString();
    }

    private String getClientIP(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
