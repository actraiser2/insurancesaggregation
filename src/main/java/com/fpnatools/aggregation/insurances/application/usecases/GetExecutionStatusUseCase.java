package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;

public interface GetExecutionStatusUseCase {

	public AggregationResultDTO getExecutionStatus(Long executionId);
}
