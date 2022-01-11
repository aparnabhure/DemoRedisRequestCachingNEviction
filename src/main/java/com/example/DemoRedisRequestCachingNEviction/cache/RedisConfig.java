package com.example.DemoRedisRequestCachingNEviction.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(
        prefix = "redis.cache.cluster"
)
@Getter
@Setter
public class RedisConfig {
    private String address;
    private String password;
    private int timeout;
    private boolean clustered;
    private boolean withSSL;
}
