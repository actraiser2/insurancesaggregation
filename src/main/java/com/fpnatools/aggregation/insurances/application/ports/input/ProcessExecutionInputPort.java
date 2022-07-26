package com.fpnatools.aggregation.insurances.application.ports.input;


import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fpnatools.aggregation.insurances.application.ports.output.CacheOutputPort;
import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.application.usecases.ProcessExecutionUseCase;
import com.fpnatools.aggregation.insurances.domain.entity.Execution;
import com.fpnatools.aggregation.insurances.domain.vo.AggregationResult;
import com.fpnatools.aggregation.insurances.domain.vo.ExecutionStatus;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionDTO;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;

import lombok.AllArgsConstructor;

@Service("ProcessExecutionUseCase")
@AllArgsConstructor
public class ProcessExecutionInputPort implements ProcessExecutionUseCase {

	private static Logger logger = LoggerFactory.getLogger(ProcessExecutionInputPort.class);
	
	private ApplicationContext applicationContext;
	private ExecutionRepository executionRepository;
	private CacheOutputPort cacheAdapter;
	
	@Override
	@Transactional
	public void processExecution(ExecutionDTO execution) {
		// TODO Auto-generated method stub
		
		
		RobotOutputPort robotAdapter = applicationContext.
				getBean(execution.getEntityName() + "Adapter", RobotOutputPort.class);
		
		Optional<Execution> executionEntity = executionRepository.
				findById(execution.getExecutionId());
		executionEntity.ifPresent(e -> {
			logger.info("Processing new execution:" + execution);
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			AggregationResult result = new AggregationResult();
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
				
				//Closing webdriver
				robotAdapter.releaseResources();
				
				stopWatch.stop();
				saveExecution(e, status, errorDescription, stopWatch.getTotalTimeSeconds());
			}
			logger.info("Execution " + execution.getExecutionId() + " finished:" + status + " =>" +
					stopWatch.getTotalTimeSeconds() + " Seconds");
			
		});
	}
	
	private void saveExecution(Execution entity, ExecutionStatus status, 
			String errorDescription, Double totalDuration) {
		entity.setExecutionStatus(status);
		entity.setErrorDescription(errorDescription);
		entity.setTotalDuration(totalDuration);
		executionRepository.save(entity);
	}

}
