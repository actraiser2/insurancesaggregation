package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.domain.events.CreatedExecutionEvent;

public interface ProcessExecutionUseCase {

	public void processExecution(CreatedExecutionEvent data);
	
}
