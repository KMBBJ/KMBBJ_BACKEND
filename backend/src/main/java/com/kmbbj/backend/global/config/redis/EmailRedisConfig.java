package com.kmbbj.backend.global.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class EmailRedisConfig {

    @Value("${REDIS_HOST}")
    private String host;

    @Value("${REDIS_PORT}")
    private Integer port;

    @Value("${REDIS_PASSWORD}")
    private String password;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        System.out.println("Connecting to Redis at " + host + ":" + port);  // 로그 추가
        System.out.println("Using password: " + password);
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setPassword(password);
        return new LettuceConnectionFactory(configuration);
    }

    /*redis 연결 방식 지정 json을 주로 사용*/
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // redis 키 직렬화 방식
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        // redis 값 직렬화 방식
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        // redis 키 해시로 직렬화
        template.setHashKeySerializer(new GenericToStringSerializer<>(String.class));
        // redus 값 해시로 직렬화
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }
}
