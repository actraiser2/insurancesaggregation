package com.fpnatools.aggregation.insurances.application.ports.input;

import org.springframework.stereotype.Service;

import com.fpnatools.aggregation.insurances.application.ports.output.CacheOutputPort;
import com.fpnatools.aggregation.insurances.application.usecases.GetExecutionStatusUseCase;
import com.fpnatools.aggregation.insurances.domain.vo.AggregationResult;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetExecutionStatusInputPort implements GetExecutionStatusUseCase {

	private CacheOutputPort cacheAdapter;
	@Override
	public AggregationResult getExecutionStatus(Long executionId) {
		// TODO Auto-generated method stub
		return cacheAdapter.getAggregationResult(executionId);
	}

}
