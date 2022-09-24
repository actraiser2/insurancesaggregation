package com.fpnatools.aggregation.insurances.framework.adapters.input;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.Execution;
import com.fpnatools.aggregation.insurances.framework.model.graphql.ExecutionQueryResolver;
import com.fpnatools.aggregation.insurances.framework.model.graphql.ExecutionType;
import com.fpnatools.aggregation.insurances.framework.model.graphql.ExecutionsQueryResolver;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;

import lombok.AllArgsConstructor;


@Controller
@AllArgsConstructor

public class GraphQLAdapter implements ExecutionsQueryResolver, ExecutionQueryResolver {

	private ExecutionRepository executionRepository;
	
	@QueryMapping
	public ExecutionType execution(@Argument Integer executionId) {
		Execution execution = executionRepository.findById(executionId.longValue()).get();
		
		return ExecutionType.builder().
				setId(executionId + "").
				setCompanyName(execution.getInsuranceCompanyEntity().getName()).
				setExecutionStatus(execution.getExecutionStatus() + "").
				setUsername(execution.getUsername()).
				setTotalDuration(execution.getTotalDuration()).
				setExecutionDate(execution.getTimestamp().
						format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))).build();
	}
	
	@QueryMapping
	public List<ExecutionType> executions(@Argument Integer size, @Argument String fromDate, 
			@Argument String toDate){
		LocalDate _fromDate = LocalDate.parse(fromDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate _toDate = LocalDate.parse(toDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
		List<ExecutionType> executions = executionRepository.findByTimestampBetween(_fromDate.atStartOfDay(), _toDate.atStartOfDay(), PageRequest.of(0, 
				size != null ? size : 10, Sort.by(Direction.DESC, "timestamp"))).
			stream().
			filter(e -> e.getInsuranceCompanyEntity() != null).
			map(e -> {
				return ExecutionType.builder().
						setId(e.getId() + "").
						setCompanyName(e.getInsuranceCompanyEntity().getName()).
						setExecutionStatus(e.getExecutionStatus() + "").
						setUsername(e.getUsername()).
						setTotalDuration(e.getTotalDuration()).
						setExecutionDate(e.getTimestamp().
								format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))).build();
				
			}).toList();
		
		return executions;
	}


}
