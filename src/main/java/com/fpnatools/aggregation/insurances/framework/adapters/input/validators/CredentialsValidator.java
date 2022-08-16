package com.fpnatools.aggregation.insurances.framework.adapters.input.validators;

import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CredentialsValidator implements ConstraintValidator<Credentials, Map<String, String>> {

	@Override
	public boolean isValid(Map<String, String> value, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		return value != null && value.containsKey("username") && value.containsKey("password");
	}

	
}
