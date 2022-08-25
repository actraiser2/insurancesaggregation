package com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.Data;


@Data
public abstract class AbstractInsurance {
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
	private List<Coverage> coverages;
}
