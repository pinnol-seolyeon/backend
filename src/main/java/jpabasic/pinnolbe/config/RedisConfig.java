package jpabasic.pinnolbe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jpabasic.pinnolbe.domain.redis.StudySession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.TimeZone;

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
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return mapper;
    }

    @Bean
    public RedisTemplate<String, StudySession> redisTemplate() {
        RedisTemplate<String,StudySession> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

//        ObjectMapper objectMapper=new ObjectMapper()
//                .registerModule(new JavaTimeModule()) //LocalDateTime 처리
//                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer=
                new GenericJackson2JsonRedisSerializer(objectMapper());

        //문자열을 redis에 저장할 때 UTF-8 문자열로 직렬화/역직렬화함(원래는 byte로 변환)
        template.setKeySerializer(new StringRedisSerializer());
        //StudySession을 JSON으로 변환해서 저장, 다시 꺼낼 때 json -> 객체로 복원
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
