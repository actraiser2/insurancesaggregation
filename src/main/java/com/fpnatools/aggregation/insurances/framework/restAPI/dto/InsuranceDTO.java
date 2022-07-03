package com.fpnatools.aggregation.insurances.framework.restAPI.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.Data;


@Data
public abstract class InsuranceDTO {
	private String productName;
	private String productId;
	private Double premium;
	private String recurrence;
	@JsonSerialize(using = LocalDateSerializer.class) 
	@JsonDeserialize(using = LocalDateDeserializer.class) 
	private LocalDate startingDate;
	@JsonSerialize(using = LocalDateSerializer.class) 
	@JsonDeserialize(using = LocalDateDeserializer.class) 
	private LocalDate dueDate;
	private String iban;
	private List<CoverageDTO> coverages;
	private HomeDTO home;
}
