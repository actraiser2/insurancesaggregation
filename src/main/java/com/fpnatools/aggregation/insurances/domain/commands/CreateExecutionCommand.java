package com.fpnatools.aggregation.insurances.domain.commands;

import java.util.Map;

import com.fpnatools.aggregation.insurances.domain.model.entities.AppUser;
import com.fpnatools.aggregation.insurances.domain.model.entities.InsuranceCompany;

import lombok.Data;

@Data
public class CreateExecutionCommand {
	private String entityName;
	private Map<String, String> credentials;
	private InsuranceCompany insuranceCompany;
	private AppUser appUser;
}
