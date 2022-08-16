package com.fpnatools.aggregation.insurances.framework.adapters.input.dto;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

import com.fpnatools.aggregation.insurances.framework.adapters.input.validators.Credentials;

import lombok.Data;

@Data
public class ExecutionRequestDTO {

	@NotEmpty(message = "entityName can not be empty")
	private String entityName;
	@NotEmpty(message = "credentials can not be empty")
	@Credentials
	private Map<String, String> credentials;
	private Long executionId;
}
