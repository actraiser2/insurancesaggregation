package com.fpnatools.aggregation.insurances.application.usecases;

import com.fpnatools.aggregation.insurances.domain.commands.CreateExecutionCommand;

public interface CreateExecutionUseCase {

	public Long createExecution(String appUser, CreateExecutionCommand command);
}
