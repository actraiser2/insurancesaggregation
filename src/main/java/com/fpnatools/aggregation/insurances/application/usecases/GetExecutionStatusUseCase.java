package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.AggregationResult;

public interface GetExecutionStatusUseCase {

	public AggregationResult getExecutionStatus(Long executionId);
}
