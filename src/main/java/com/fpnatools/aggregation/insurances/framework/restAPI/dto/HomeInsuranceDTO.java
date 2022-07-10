package com.fpnatools.aggregation.insurances.framework.restAPI.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HomeInsuranceDTO extends InsuranceDTO {

	private String rawInsurancedHomeAddress;
	private String propertyRegistryNumber;
	private String constructionYear;
	private String squaredMetres;
	private String houseType;
	private String homeAdditionalDetail;
}
