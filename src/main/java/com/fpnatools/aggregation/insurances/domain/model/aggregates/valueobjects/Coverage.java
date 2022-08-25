package com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coverage {

	private String name;
	private Double amount;
}
