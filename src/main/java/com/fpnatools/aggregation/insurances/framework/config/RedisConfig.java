package com.fpnatools.aggregation.insurances.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;

@Configuration
public class RedisConfig {

	@Bean 
	public RedisTemplate<String, AggregationResultDTO> redisOperations(RedisConnectionFactory factory){
		 
		Jackson2JsonRedisSerializer<AggregationResultDTO> serializer =
	               new Jackson2JsonRedisSerializer<>(AggregationResultDTO.class);

        RedisTemplate<String, AggregationResultDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());
        
        return template;
        
	}
}
