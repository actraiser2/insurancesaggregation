package com.fpnatools.aggregation.insurances.framework.restAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoverageDTO {

	private String name;
	private Double amount;
}
