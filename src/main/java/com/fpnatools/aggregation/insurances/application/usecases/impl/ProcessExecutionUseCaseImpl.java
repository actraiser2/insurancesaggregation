package com.fpnatools.aggregation.insurances.application.usecases.impl;

import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fpnatools.aggregation.insurances.application.usecases.ProcessExecutionUseCase;
import com.fpnatools.aggregation.insurances.domain.entity.ExecutionEntity;
import com.fpnatools.aggregation.insurances.domain.entity.vo.ExecutionStatus;
import com.fpnatools.aggregation.insurances.framework.adapters.CacheAdapter;
import com.fpnatools.aggregation.insurances.framework.adapters.RobotAdapter;
import com.fpnatools.aggregation.insurances.framework.repository.ExecutionRepository;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.ExecutionRequestDTO;

import lombok.AllArgsConstructor;

@Service("ProcessExecutionUseCase")
@AllArgsConstructor
public class ProcessExecutionUseCaseImpl implements ProcessExecutionUseCase {

	private static Logger logger = LoggerFactory.getLogger(ProcessExecutionUseCaseImpl.class);
	
	private ApplicationContext applicationContext;
	private ExecutionRepository executionRepository;
	private CacheAdapter cacheAdapter;
	
	@Override
	@Transactional
	public void processExecution(ExecutionRequestDTO execution) {
		// TODO Auto-generated method stub
		
		
		RobotAdapter robotAdapter = applicationContext.
				getBean(execution.getEntityName() + "Adapter", RobotAdapter.class);
		
		Optional<ExecutionEntity> executionEntity = executionRepository.
				findById(execution.getExecutionId());
		executionEntity.ifPresent(e -> {
			logger.info("Processing new execution:" + execution);
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			AggregationResultDTO result = new AggregationResultDTO();
			result.setExecutionId(execution.getExecutionId());
			
			ExecutionStatus status = ExecutionStatus.ONGOING;
			String errorDescription = "";
		
			try{
				boolean logged = robotAdapter.login(execution.getCredentials());
				
				if (logged) {
				
					result.setPersonalInformation(robotAdapter.getPersonalInformation());
					result.setHomeInsurances(robotAdapter.getHomeInsurances());
					result.setCarInsurances(robotAdapter.getCarInsurances());
					status = ExecutionStatus.FINISH_OK;
				}
				else {
					status = ExecutionStatus.INVALID_LOGIN;
				}
				
				
				
			}
			catch(Exception ex) {
				logger.error("Error in " + execution.getEntityName() + ":", ex);
				status = ExecutionStatus.EXECUTION_FAILED;
				errorDescription = ex.getClass() + " => " + ex.getMessage();
			}
			finally {
				result.setExecutionStatus(status);
				result.setErrorDescription(errorDescription);
				cacheAdapter.cacheAggregationResult(result);
				
				stopWatch.stop();
				saveExecution(e, status, errorDescription, stopWatch.getTotalTimeSeconds());
			}
			logger.info("Execution " + execution.getExecutionId() + " finished:" + status + " =>" +
					stopWatch.getTotalTimeSeconds() + " Seconds");
			
		});
	}
	
	private void saveExecution(ExecutionEntity entity, ExecutionStatus status, 
			String errorDescription, Double totalDuration) {
		entity.setExecutionStatus(status);
		entity.setErrorDescription(errorDescription);
		entity.setTotalDuration(totalDuration);
		executionRepository.save(entity);
	}

}
