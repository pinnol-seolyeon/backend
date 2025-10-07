package jpabasic.pinnolbe.config;

import jpabasic.pinnolbe.domain.StudySession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host,port);
    }

    @Bean
    public RedisTemplate<String, StudySession> redisTemplate() {
        RedisTemplate<String,StudySession> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        //문자열을 redis에 저장할 때 UTF-8 문자열로 직렬화/역직렬화함(원래는 byte로 변환)
        template.setKeySerializer(new StringRedisSerializer());
        //StudySession을 JSON으로 변환해서 저장, 다시 꺼낼 때 json -> 객체로 복원
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
