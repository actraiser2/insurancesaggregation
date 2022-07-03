package com.fpnatools.aggregation.insurances.framework.restAPI.dto;

import java.util.List;

import com.fpnatools.aggregation.insurances.domain.entity.vo.ExecutionStatus;

import lombok.Data;

@Data
public class AggregationResultDTO {

	private Long executionId;
	private ExecutionStatus executionStatus;
	private String errorDescription;
	private PersonalInformationDTO personalInformation;
	private List<HomeInsuranceDTO> homeInsurances;
	private List<CarInsuranceDTO> carInsurances;
}
