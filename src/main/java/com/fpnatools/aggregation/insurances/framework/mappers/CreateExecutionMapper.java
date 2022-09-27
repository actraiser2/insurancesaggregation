package com.fpnatools.aggregation.insurances.framework.mappers;

import org.mapstruct.Mapper;

import com.fpnatools.aggregation.insurances.domain.commands.CreateExecutionCommand;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.CreateExecutionDTO;

@Mapper(componentModel = "spring") 
public interface CreateExecutionMapper {

	public CreateExecutionCommand map(CreateExecutionDTO createExecutionDTO); 
}
