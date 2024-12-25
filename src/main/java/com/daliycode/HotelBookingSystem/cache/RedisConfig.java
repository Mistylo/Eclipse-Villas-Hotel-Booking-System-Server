package com.daliycode.HotelBookingSystem.cache;

import com.daliycode.HotelBookingSystem.model.Room;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Room> redisTemplate() {
        RedisTemplate<String, Room> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Room.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Room.class));
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 这里使用 Lettuce 连接工厂，你也可以使用 Jedis 连接工厂
        return new LettuceConnectionFactory();
    }
}

