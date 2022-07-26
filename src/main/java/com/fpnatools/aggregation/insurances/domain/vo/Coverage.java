package com.fpnatools.aggregation.insurances.domain.vo;

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
