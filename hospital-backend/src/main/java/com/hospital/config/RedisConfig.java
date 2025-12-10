package com.hospital.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

/**
 * Redis配置类
 * 只有当Redis可用时才启用
 * Redis将使用Spring Boot自动配置，无需启用Redis Repositories
 * 如果Redis连接失败，应用仍可启动，但Redis功能不可用
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisConfig {
    
    private final Optional<RedisConnectionFactory> redisConnectionFactory;
    
    public RedisConfig(Optional<RedisConnectionFactory> redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }
    
    @PostConstruct
    public void checkRedisConnection() {
        redisConnectionFactory.ifPresentOrElse(
            factory -> {
                try {
                    factory.getConnection().ping();
                    log.info("Redis连接成功");
                } catch (Exception e) {
                    log.warn("Redis连接失败，应用将继续启动但Redis功能不可用: {}", e.getMessage());
                }
            },
            () -> log.warn("Redis连接工厂未配置，Redis功能不可用")
        );
    }
}


