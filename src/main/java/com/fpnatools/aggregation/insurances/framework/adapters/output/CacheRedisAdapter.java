package com.fpnatools.aggregation.insurances.framework.adapters.output;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.application.ports.output.CacheOutputPort;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.AggregationResult;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CacheRedisAdapter implements CacheOutputPort {

	private RedisTemplate<String, AggregationResult> redisTemplate;
	
	@Override
	public void cacheAggregationResult(AggregationResult result) {
		// TODO Auto-generated method stub
		redisTemplate.opsForValue().set(result.getExecutionId() + "", result, 
				5, TimeUnit.MINUTES);
	}

	@Override
	public AggregationResult getAggregationResult(Long executionId) {
		// TODO Auto-generated method stub
		return redisTemplate.opsForValue().get(executionId + "");
	}
	
	

}
