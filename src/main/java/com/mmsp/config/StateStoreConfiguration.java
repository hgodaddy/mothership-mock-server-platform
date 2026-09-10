package com.mmsp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmsp.service.InMemoryStateStore;
import com.mmsp.service.RedisStateStore;
import com.mmsp.service.StateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.URI;
import java.time.Duration;

@Configuration
public class StateStoreConfiguration {

    private static final Logger log = LoggerFactory.getLogger(StateStoreConfiguration.class);

    @Bean
    @ConditionalOnProperty(prefix = "mmsp", name = "redis-enabled", havingValue = "false", matchIfMissing = true)
    StateStore inMemoryStateStore() {
        log.info("State backend: in-memory (REDIS_ENABLED=false)");
        return new InMemoryStateStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "mmsp", name = "redis-enabled", havingValue = "true")
    LettuceConnectionFactory redisConnectionFactory(MmspProperties properties) {
        URI uri = URI.create(properties.getRedisUrl());
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(uri.getHost() == null ? "localhost" : uri.getHost());
        standalone.setPort(uri.getPort() > 0 ? uri.getPort() : 6379);
        if (uri.getUserInfo() != null && uri.getUserInfo().contains(":")) {
            String[] parts = uri.getUserInfo().split(":", 2);
            if (!parts[1].isBlank()) {
                standalone.setPassword(parts[1]);
            }
        }
        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(3))
                .build();
        log.info("State backend: Redis at {}:{}", standalone.getHostName(), standalone.getPort());
        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, client);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    @ConditionalOnProperty(prefix = "mmsp", name = "redis-enabled", havingValue = "true")
    StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "mmsp", name = "redis-enabled", havingValue = "true")
    StateStore redisStateStore(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        return new RedisStateStore(stringRedisTemplate, objectMapper);
    }
}
