package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.framework.restAPI.dto.ExecutionRequestDTO;

public interface CreateExecutionUseCase {

	public Long createExecution(String appUser, ExecutionRequestDTO executionDTO);
}
