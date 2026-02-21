/*
package com.dge.rag_chat_service.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisRateLimitConfig {

    @Bean
    public ProxyManager<String> proxyManager(LettuceConnectionFactory factory) {

        StatefulRedisConnection<String, byte[]> connection =
                (StatefulRedisConnection<String, byte[]>) factory.getConnection();

        return LettuceBasedProxyManager.builderFor(connection).build();
    }
}
*/
