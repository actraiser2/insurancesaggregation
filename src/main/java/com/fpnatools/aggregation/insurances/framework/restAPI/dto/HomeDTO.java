package com.fpnatools.aggregation.insurances.framework.restAPI.dto;

import lombok.Data;

@Data
public class HomeDTO {
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
