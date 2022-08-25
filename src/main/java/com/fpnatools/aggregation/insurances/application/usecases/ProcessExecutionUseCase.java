package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.domain.events.CreatedExecutionEventData;

public interface ProcessExecutionUseCase {

	public void processExecution(CreatedExecutionEventData data);
	
}
