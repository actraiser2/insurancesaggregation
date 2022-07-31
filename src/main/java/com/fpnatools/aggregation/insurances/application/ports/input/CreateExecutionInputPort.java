package com.fpnatools.aggregation.insurances.application.ports.input;

import java.util.Optional;

import org.springframework.cloud.sleuth.Tracer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fpnatools.aggregation.insurances.application.ports.output.CacheOutputPort;
import com.fpnatools.aggregation.insurances.application.usecases.CreateExecutionUseCase;
import com.fpnatools.aggregation.insurances.domain.entity.AppUser;
import com.fpnatools.aggregation.insurances.domain.entity.Execution;
import com.fpnatools.aggregation.insurances.domain.entity.InsuranceCompany;
import com.fpnatools.aggregation.insurances.domain.vo.AggregationResult;
import com.fpnatools.aggregation.insurances.domain.vo.ExecutionStatus;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionDTO;
import com.fpnatools.aggregation.insurances.framework.exceptions.InsuranceCompanyNotFoundException;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.InsuranceCompanyRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@AllArgsConstructor
@Log4j2
public class CreateExecutionInputPort implements CreateExecutionUseCase {

	private ExecutionRepository executionRepository;
	private UserRepository userRepository;
	private InsuranceCompanyRepository insuranceCompanyRepository;
	private MessageChannel executionsChannel;
	private CacheOutputPort cacheAdapter;
	private Tracer tracer;
	
	
	@Override
	public Long createExecution(String appUser, ExecutionDTO executionDTO) {
		// TODO Auto-generated method stub
		AppUser userEntity = userRepository.findByAppUser(appUser).get();
		Optional<InsuranceCompany> insuranceCompanyEntity = insuranceCompanyRepository.findByName(executionDTO.getEntityName());
		
		if (insuranceCompanyEntity.isPresent()) {
			Execution executionEntity = new Execution();
			executionEntity.setExecutionStatus(ExecutionStatus.ONGOING);
			executionEntity.setUser(userEntity);
			executionEntity.setUsername(executionDTO.getCredentials().get("username"));
			executionEntity.setInsuranceCompanyEntity(insuranceCompanyEntity.get());
			executionEntity.setTraceId(tracer.currentSpan().context().traceId());
			
			executionRepository.save(executionEntity);
			
			executionDTO.setExecutionId(executionEntity.getId());
	
			executionsChannel.send(MessageBuilder.withPayload(executionDTO).build());
			
			var result = new AggregationResult();
			result.setExecutionId(executionEntity.getId());
			result.setExecutionStatus(ExecutionStatus.ONGOING);
			cacheAdapter.cacheAggregationResult(result);
			
			return executionEntity.getId();
		}
		else {
			throw new InsuranceCompanyNotFoundException();
		}
		
		
		
		
	}

}
