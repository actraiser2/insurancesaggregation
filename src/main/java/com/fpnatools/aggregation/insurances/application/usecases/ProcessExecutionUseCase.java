package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionDTO;

public interface ProcessExecutionUseCase {

	public void processExecution(ExecutionDTO execution);
	
}
