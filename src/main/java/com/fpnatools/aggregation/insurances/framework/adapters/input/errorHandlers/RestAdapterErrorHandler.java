package com.fpnatools.aggregation.insurances.framework.adapters.input.errorHandlers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ErrorDTO;
import com.fpnatools.aggregation.insurances.framework.exceptions.GenericAggregationException;
import com.fpnatools.aggregation.insurances.framework.exceptions.InsuranceCompanyNotFoundException;

@ControllerAdvice
@RestController
public class RestAdapterErrorHandler {

	@ExceptionHandler(InsuranceCompanyNotFoundException.class)
	public ErrorDTO handlerInsuranceCompanyNotFoundException(InsuranceCompanyNotFoundException ex) {
		return new ErrorDTO("Insurance Company not recognized");
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	public ErrorDTO handlerValidationException(MethodArgumentNotValidException ex) {
		return new ErrorDTO("Bad request: " + ex.getAllErrors().get(0).getDefaultMessage());
	}
	
	@ExceptionHandler(GenericAggregationException.class)
	public ErrorDTO handleGenericAggregationException(GenericAggregationException ex) {
		return new ErrorDTO("Unknow error: " + ex.getMessage());
	}
}
