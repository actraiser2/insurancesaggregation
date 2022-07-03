package com.fpnatools.aggregation.insurances.application.usecases.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fpnatools.aggregation.insurances.application.usecases.CreateExecutionUseCase;
import com.fpnatools.aggregation.insurances.domain.entity.ExecutionEntity;
import com.fpnatools.aggregation.insurances.domain.entity.InsuranceCompanyEntity;
import com.fpnatools.aggregation.insurances.domain.entity.UserEntity;
import com.fpnatools.aggregation.insurances.domain.entity.vo.ExecutionStatus;
import com.fpnatools.aggregation.insurances.framework.adapters.CacheAdapter;
import com.fpnatools.aggregation.insurances.framework.exceptions.InsuranceCompanyNotFoundException;
import com.fpnatools.aggregation.insurances.framework.repository.ExecutionRepository;
import com.fpnatools.aggregation.insurances.framework.repository.InsuranceCompanyRepository;
import com.fpnatools.aggregation.insurances.framework.repository.UserRepository;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.ExecutionRequestDTO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CreateExecutionUseCaseImpl implements CreateExecutionUseCase {

	private ExecutionRepository executionRepository;
	private UserRepository userRepository;
	private InsuranceCompanyRepository insuranceCompanyRepository;
	private MessageChannel executionsChannel;
	private CacheAdapter cacheAdapter;
	private Tracer tracer;
	private static Logger logger = LoggerFactory.getLogger(CreateExecutionUseCaseImpl.class);
	
	
	@Override
	public Long createExecution(String appUser, ExecutionRequestDTO executionDTO) {
		// TODO Auto-generated method stub
		UserEntity userEntity = userRepository.findByAppUser(appUser).get();
		Optional<InsuranceCompanyEntity> insuranceCompanyEntity = insuranceCompanyRepository.findByName(executionDTO.getEntityName());
		
		if (insuranceCompanyEntity.isPresent()) {
			ExecutionEntity executionEntity = new ExecutionEntity();
			executionEntity.setExecutionStatus(ExecutionStatus.ONGOING);
			executionEntity.setUser(userEntity);
			executionEntity.setUsername(executionDTO.getCredentials().get("username"));
			executionEntity.setInsuranceCompanyEntity(insuranceCompanyEntity.get());
			executionEntity.setTraceId(tracer.currentSpan().context().traceId());
			
			executionRepository.save(executionEntity);
			
			executionDTO.setExecutionId(executionEntity.getId());
	
			executionsChannel.send(MessageBuilder.withPayload(executionDTO).build());
			
			var result = new AggregationResultDTO();
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
