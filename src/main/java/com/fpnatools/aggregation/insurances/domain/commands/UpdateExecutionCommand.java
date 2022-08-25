package com.fpnatools.aggregation.insurances.domain.commands;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.ExecutionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateExecutionCommand {

	private ExecutionStatus executionStatus;
	private String errorDescription;
	private Double totalDuration;
}
