package com.fpnatools.aggregation.insurances.framework.adapters.input;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.Execution;
import com.fpnatools.aggregation.insurances.framework.model.graphql.ExecutionGraphQL;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;

import lombok.AllArgsConstructor;


@Controller
@AllArgsConstructor
public class GraphQLAdapter {

	private ExecutionRepository executionRepository;
	
	@QueryMapping
	public ExecutionGraphQL execution(@Argument Long executionId) {
		Execution execution = executionRepository.findById(executionId).get();
		
		return ExecutionGraphQL.builder().
				setId(executionId + "").
				setCompanyName(execution.getInsuranceCompanyEntity().getName()).
				setExecutionStatus(execution.getExecutionStatus() + "").
				setUsername(execution.getUsername()).
				setTotalDuration(execution.getTotalDuration()).
				setExecutionDate(execution.getTimestamp().
						format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).build();
	}
}
