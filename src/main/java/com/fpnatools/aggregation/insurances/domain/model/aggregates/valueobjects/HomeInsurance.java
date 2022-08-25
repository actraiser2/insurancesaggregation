package com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HomeInsurance extends AbstractInsurance {

	private String propertyRegistryNumber;
	private String constructionYear;
	private String squaredMetres;
	private String houseType;
	private String homeAdditionalDetail;
	private Home asseguredHome;
}
