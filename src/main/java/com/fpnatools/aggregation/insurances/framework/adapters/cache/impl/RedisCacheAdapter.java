package com.fpnatools.aggregation.insurances.framework.adapters.cache.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fpnatools.aggregation.insurances.framework.adapters.CacheAdapter;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RedisCacheAdapter implements CacheAdapter {

	private RedisTemplate<String, AggregationResultDTO> redisTemplate;
	
	@Override
	public void cacheAggregationResult(AggregationResultDTO result) {
		// TODO Auto-generated method stub
		redisTemplate.opsForValue().set(result.getExecutionId() + "", result, 
				5, TimeUnit.MINUTES);
	}

	@Override
	public AggregationResultDTO getAggregationResult(Long executionId) {
		// TODO Auto-generated method stub
		return redisTemplate.opsForValue().get(executionId + "");
	}
	
	

}
