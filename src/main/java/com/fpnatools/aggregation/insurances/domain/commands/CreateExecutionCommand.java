package com.fpnatools.aggregation.insurances.domain.commands;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

import com.fpnatools.aggregation.insurances.domain.model.entities.AppUser;
import com.fpnatools.aggregation.insurances.domain.model.entities.InsuranceCompany;
import com.fpnatools.aggregation.insurances.framework.adapters.input.validators.Credentials;

import lombok.Data;

@Data
public class CreateExecutionCommand {

	@NotEmpty(message = "entityName can not be empty")
	private String entityName;
	@NotEmpty(message = "credentials can not be empty")
	@Credentials
	private Map<String, String> credentials;
	private InsuranceCompany insuranceCompany;
	private AppUser appUser;
}
