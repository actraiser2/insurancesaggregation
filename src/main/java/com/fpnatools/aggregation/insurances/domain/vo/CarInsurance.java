package com.fpnatools.aggregation.insurances.domain.vo;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fpnatools.aggregation.insurances.domain.vo.enums.CoverType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CarInsurance extends AbstractInsurance {
	private String mainDriverNif;
	private String mainDriverName;
	@JsonSerialize(using = LocalDateSerializer.class) 
	@JsonDeserialize(using = LocalDateDeserializer.class) 
	private LocalDate mainDriverBirthDate; 
	private Integer mainDriverAgeOfCarnet;
	private String brand;
	private String model;
	private String extendedModelInfo;
	private String carPlate;
	private String vehicleType;
	private CoverType coverType;
	private Integer registrationYear;
}
