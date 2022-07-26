package com.fpnatools.aggregation.insurances.framework.adapters.input.dto;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class ExecutionDTO {

	@NotEmpty
	private String entityName;
	@NotEmpty
	private Map<String, String> credentials;
	private Long executionId;
}
