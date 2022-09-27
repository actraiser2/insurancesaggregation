package com.fpnatools.aggregation.insurances.application.ports.input;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.fpnatools.aggregation.insurances.application.ports.output.CacheOutputPort;
import com.fpnatools.aggregation.insurances.application.usecases.CreateExecutionUseCase;
import com.fpnatools.aggregation.insurances.domain.commands.CreateExecutionCommand;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.Execution;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.AggregationResult;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.ExecutionStatus;
import com.fpnatools.aggregation.insurances.domain.model.entities.AppUser;
import com.fpnatools.aggregation.insurances.domain.model.entities.InsuranceCompany;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.CreateExecutionDTO;
import com.fpnatools.aggregation.insurances.framework.exceptions.InsuranceCompanyNotFoundException;
import com.fpnatools.aggregation.insurances.framework.mappers.CreateExecutionMapper;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.InsuranceCompanyRepository;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class CreateExecutionInputPort implements CreateExecutionUseCase {

	private ExecutionRepository executionRepository;
	private UserRepository userRepository;
	private InsuranceCompanyRepository insuranceCompanyRepository;
	private CacheOutputPort cacheAdapter;
	private CreateExecutionMapper createExecutionMapper;
	
	
	@Override
	@Transactional
	public Long createExecution(String appUser, CreateExecutionDTO createExecutionDTO) {
		// TODO Auto-generated method stub
		AppUser userEntity = userRepository.findByAppUser(appUser).get();
		Optional<InsuranceCompany> insuranceCompanyEntity = insuranceCompanyRepository.findByName(createExecutionDTO.getEntityName());
		
		log.info("New request for " + createExecutionDTO.getEntityName());

		if (insuranceCompanyEntity.isPresent()) {
			CreateExecutionCommand command = createExecutionMapper.map(createExecutionDTO);
			
			command.setAppUser(userEntity);
			command.setInsuranceCompany(insuranceCompanyEntity.get());
			Execution execution = new Execution(command);
			
			executionRepository.save(execution);
			
			//sETTING the initial status of the execution as ONGOING for the client
			var result = new AggregationResult();
			result.setExecutionId(execution.getId());
			result.setExecutionStatus(ExecutionStatus.ONGOING);
			cacheAdapter.cacheAggregationResult(result);
			
			return execution.getId();
		}
		else {
			throw new InsuranceCompanyNotFoundException();
		}
		
		
		
		
	}

}
