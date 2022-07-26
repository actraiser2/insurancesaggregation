package com.fpnatools.aggregation.insurances.framework.adapters.input.errorHandlers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.fpnatools.aggregation.insurances.framework.exceptions.InsuranceCompanyNotFoundException;

@ControllerAdvice
@RestController
public class RestAdapterErrorHandler {

	@ExceptionHandler(InsuranceCompanyNotFoundException.class)
	public Error handlerInsuranceCompanyNotFoundException(InsuranceCompanyNotFoundException ex) {
		return new Error("Insurance Company not recognized");
	}
}
