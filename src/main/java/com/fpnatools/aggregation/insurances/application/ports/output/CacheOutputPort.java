package com.fpnatools.aggregation.insurances.application.ports.output;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.AggregationResult;

public interface CacheOutputPort {

	public void cacheAggregationResult(AggregationResult result);
	public AggregationResult getAggregationResult(Long executionId);
}
