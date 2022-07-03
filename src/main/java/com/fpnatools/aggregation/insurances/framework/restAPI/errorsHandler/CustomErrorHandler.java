package com.fpnatools.aggregation.insurances.framework.restAPI.errorsHandler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.fpnatools.aggregation.insurances.framework.exceptions.InsuranceCompanyNotFoundException;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.ErrorDTO;

@ControllerAdvice
@RestController
public class CustomErrorHandler {

	@ExceptionHandler(InsuranceCompanyNotFoundException.class)
	public ErrorDTO handlerInsuranceCompanyNotFoundException(InsuranceCompanyNotFoundException ex) {
		return new ErrorDTO("Insurance Company not recognized");
	}
}
