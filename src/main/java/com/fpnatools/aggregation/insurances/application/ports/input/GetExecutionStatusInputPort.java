package com.fpnatools.aggregation.insurances.application.ports.input;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fpnatools.aggregation.insurances.application.ports.output.CacheOutputPort;
import com.fpnatools.aggregation.insurances.application.usecases.GetExecutionStatusUseCase;
import com.fpnatools.aggregation.insurances.domain.vo.AggregationResult;
import com.fpnatools.aggregation.insurances.framework.persistence.repository.ExecutionRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class GetExecutionStatusInputPort implements GetExecutionStatusUseCase {

	private CacheOutputPort cacheAdapter;
	private ExecutionRepository executionRepository;
	
	@Override
	public AggregationResult getExecutionStatus(Long executionId) {
		// TODO Auto-generated method stub
		log.info("Before querying all");
		executionRepository.findAll();
		log.info("After querying all");
		return cacheAdapter.getAggregationResult(executionId);
	}

}
