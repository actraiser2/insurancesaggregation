package com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Error {

	private String errorMessage;
}
