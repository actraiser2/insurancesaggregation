package com.fpnatools.aggregation.insurances.framework.adapters;

import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;

public interface CacheAdapter {

	public void cacheAggregationResult(AggregationResultDTO result);
	public AggregationResultDTO getAggregationResult(Long executionId);
}
