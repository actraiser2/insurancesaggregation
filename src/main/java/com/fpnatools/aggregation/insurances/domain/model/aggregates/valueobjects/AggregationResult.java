package com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects;

import java.util.List;

import lombok.Data;

@Data
public class AggregationResult {

	private Long executionId;
	private ExecutionStatus executionStatus;
	private String errorDescription;
	private PersonalInformation personalInformation;
	private List<HomeInsurance> homeInsurances;
	private List<CarInsurance> carInsurances;
}
