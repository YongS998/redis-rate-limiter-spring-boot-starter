# 使用方法

## 引入jar包

## 基础配置

```yaml
  spring:
    data:
      redis:
        host: localhost
        port: 6379
        password:
        lettuce:
          pool:
            max-active: 8
            max-wait: -1ms
            max-idle: 8
            min-idle: 0
        timeout: 2000ms
  rate-limiter:
    enabled: true # 默认true
```

## 具体使用
```java
    @RestController
    @RequestMapping("/api")
    public class ApiController {
    
        @GetMapping("/public")
        @RateLimit(
                key = "public_api",window = 60,maxRequests = 5,
                type = RateLimit.LimitType.FIXED_WINDOW,
                message = "公共接口访问频率超限"
        )
        public Result publicApi(HttpServletRequest request){
            return Result.success("响应成功");
        }
    
        @GetMapping("/user")
        @RateLimit(key = "user_api", window = 60, maxRequests = 5,
                type = RateLimit.LimitType.SLIDING_WINDOW,
                message = "用户接口访问频率超限")
        public Result userApi(HttpServletRequest request) {
            return Result.success("用户接口响应成功");
        }
    
        @GetMapping("/premium")
        @RateLimit(key = "premium_api", window = 60, maxRequests = 5,
                type = RateLimit.LimitType.LEAKY_BUCKET,
                message = "高级接口访问频率超限")
        public Result premiumApi() {
            return Result.success("高级接口响应成功");
        }
    }
``` 

## 注解详细解释

```java
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RateLimit {
    
        /**
         * 限流key
         */
        String key() default "";
    
        /**
         * 时间窗口 s
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
```


