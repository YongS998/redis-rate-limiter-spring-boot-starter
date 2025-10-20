package com.yongs.limiter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 功能：
 * 作者：YongS
 * 日期：2025/10/20 20:53
 */
@ConfigurationProperties(prefix = "rate-limiter")
@Data
public class RateLimiterProperties {

    private Boolean enabled;
}
