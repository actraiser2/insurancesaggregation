package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionRequestDTO;

public interface ProcessExecutionUseCase {

	public void processExecution(ExecutionRequestDTO execution);
	
}
