package com.volunteerBackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer; 
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6381);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Make keys readable as standard Strings
        template.setKeySerializer(new StringRedisSerializer());
        
        // Use the new Jackson 3 serializer for object values
        template.setValueSerializer(GenericJacksonJsonRedisSerializer.builder().build());
        
        // Apply the same to Hash keys/values if you use Redis Hashes
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(GenericJacksonJsonRedisSerializer.builder().build());

        template.afterPropertiesSet();
        return template;
    }
}
