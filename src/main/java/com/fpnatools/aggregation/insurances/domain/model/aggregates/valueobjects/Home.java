package com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects;

import lombok.Data;

@Data
public class Home {
	private String rawAddress;
	private String city;
	private String postalCode;
	private String province;
	private String number;
	private String street;
	private String floor;
	private String door;
	private String streetType;
}
