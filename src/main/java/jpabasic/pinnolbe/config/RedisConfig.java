package jpabasic.pinnolbe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jpabasic.pinnolbe.domain.redis.StudySession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.TimeZone;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    // 1. 기본 연결 설정 (Lettuce)
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }

    // 2. 공통 serializer
    private RedisSerializer<Object> valueSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new Jackson2JsonRedisSerializer<>(Object.class);
    }

    // 3. RedisTemplate<String, Object> (Hash, ZSET 등 모든 데이터 저장용)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            LettuceConnectionFactory connectionFactory
    ) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer → String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer → JSON
        template.setValueSerializer(valueSerializer());
        template.setHashValueSerializer(valueSerializer());

        template.afterPropertiesSet();
        return template;
    }

    // 4. StringRedisTemplate (순수 String 기반 작업 시)
    @Bean
    public StringRedisTemplate stringRedisTemplate(
            LettuceConnectionFactory connectionFactory
    ) {
        return new StringRedisTemplate(connectionFactory);
    }
}

