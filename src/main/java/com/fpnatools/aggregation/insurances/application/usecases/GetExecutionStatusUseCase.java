package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.domain.vo.AggregationResult;

public interface GetExecutionStatusUseCase {

	public AggregationResult getExecutionStatus(Long executionId);
}
