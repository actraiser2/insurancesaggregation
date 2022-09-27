package com.fpnatools.aggregation.insurances.application.ports.input;


import javax.transaction.Transactional;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.fpnatools.aggregation.insurances.application.ports.output.CacheOutputPort;
import com.fpnatools.aggregation.insurances.application.ports.output.RobotOutputPort;
import com.fpnatools.aggregation.insurances.application.usecases.ProcessExecutionUseCase;
import com.fpnatools.aggregation.insurances.domain.commands.UpdateExecutionCommand;
import com.fpnatools.aggregation.insurances.domain.events.CreatedExecutionEvent;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.Execution;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.AggregationResult;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.ExecutionStatus;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service("ProcessExecutionUseCase")
@AllArgsConstructor
@Log4j2
public class ProcessExecutionInputPort implements ProcessExecutionUseCase {

	private ApplicationContext applicationContext;
	private ExecutionRepository executionRepository;
	private CacheOutputPort cacheAdapter;
	
	@Override
	@Transactional
	public void processExecution(CreatedExecutionEvent data) {
		// TODO Auto-generated method stub
		
		Long executionId = data.getExecution().getId();
		
		Execution execution = executionRepository.findById(executionId).get();
		
		RobotOutputPort robotAdapter = applicationContext.
				getBean(execution.getInsuranceCompanyEntity().getName() + "Adapter", RobotOutputPort.class);
		
		
		log.info("Processing new execution:" + execution.getInsuranceCompanyEntity().getName());
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		AggregationResult result = new AggregationResult();
		result.setExecutionId(executionId);
		
		ExecutionStatus status = ExecutionStatus.ONGOING;
		String errorDescription = "";
	
		try{
			boolean logged = robotAdapter.login(data.getCredentials());
			
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
			log.error("Error in " + execution.getInsuranceCompanyEntity().getName() + ":", ex);
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
			execution.updateExecution(new UpdateExecutionCommand(status, errorDescription, stopWatch.getTotalTimeSeconds()));
			executionRepository.save(execution);
		}
		
		log.info("Execution " + executionId + " finished:" + status + " =>" +
				stopWatch.getTotalTimeSeconds() + " Seconds");
	}
	

}
