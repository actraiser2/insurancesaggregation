package com.fpnatools.aggregation.insurances.framework.adapters.input.errorHandlers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ErrorDTO;
import com.fpnatools.aggregation.insurances.framework.exceptions.InsuranceCompanyNotFoundException;

@ControllerAdvice
@RestController
public class RestAdapterErrorHandler {

	@ExceptionHandler(InsuranceCompanyNotFoundException.class)
	public ErrorDTO handlerInsuranceCompanyNotFoundException(InsuranceCompanyNotFoundException ex) {
		return new ErrorDTO("Insurance Company not recognized");
	}
}
