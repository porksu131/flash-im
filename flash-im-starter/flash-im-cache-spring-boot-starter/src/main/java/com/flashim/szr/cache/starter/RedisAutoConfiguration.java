package com.flashim.szr.cache.starter;


import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@AutoConfiguration
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisAutoConfiguration {
    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<Object, Object> fastJsonRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Key 使用字符串序列化
        template.setKeySerializer(new StringRedisSerializer());
        // Value 使用 JSON 序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

//    @Bean
//    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        // 使用String序列化
//        StringRedisSerializer stringSerializer = new StringRedisSerializer();
//        template.setKeySerializer(stringSerializer);
//        template.setValueSerializer(stringSerializer);
//        template.setHashKeySerializer(stringSerializer);
//        template.setHashValueSerializer(stringSerializer);
//
//        return template;
//    }

    @Bean
    public RedisTemplate<String, Long> longRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 String 序列化器作为 key 的序列化器
        template.setKeySerializer(new StringRedisSerializer());

        // 自定义 Long 值的序列化器
        template.setValueSerializer(new RedisSerializer<Long>() {
            private final Charset charset = StandardCharsets.UTF_8;

            @Override
            public byte[] serialize(Long value) throws SerializationException {
                return (value == null ? null : value.toString().getBytes(charset));
            }

            @Override
            public Long deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null) return null;
                String str = new String(bytes, charset);
                return Long.parseLong(str);
            }
        });

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Map<String, Object>> mapRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Map<String, Object>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 String 序列化器作为 Key 的序列化器
        template.setKeySerializer(new StringRedisSerializer());

        // 使用 Jackson2JsonRedisSerializer 作为 Value 的序列化器（处理 Map）
        Jackson2JsonRedisSerializer<Map> valueSerializer = new Jackson2JsonRedisSerializer<>(Map.class);
        template.setValueSerializer(valueSerializer);

        // 设置 HashKey/HashValue 序列化器（可选）
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisWorkerIdAssigner redisWorkerIdAssigner(RedisTemplate<String, Long> longRedisTemplate) {
        return new RedisWorkerIdAssigner(longRedisTemplate);
    }

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(RedisWorkerIdAssigner redisWorkerIdAssigner) {
        return new SnowflakeIdGenerator(redisWorkerIdAssigner.getWorkerId());
    }

    @Bean
    public SequenceIdGenerator sequenceIdGenerator(RedisTemplate<String, Long> longRedisTemplate) {
        return new SequenceIdGenerator(longRedisTemplate);
    }

    @Bean
    public UserSessionManager userSessionManager(RedisTemplate<String, Map<String, Object>> mapRedisTemplate,
                                                 RedisTemplate<String, Object> redisTemplate) {
        return new UserSessionManager(mapRedisTemplate, redisTemplate);
    }
}
