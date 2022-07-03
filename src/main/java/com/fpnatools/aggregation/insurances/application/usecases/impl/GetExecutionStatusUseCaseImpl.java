package com.fpnatools.aggregation.insurances.application.usecases.impl;

import org.springframework.stereotype.Service;

import com.fpnatools.aggregation.insurances.application.usecases.GetExecutionStatusUseCase;
import com.fpnatools.aggregation.insurances.framework.adapters.CacheAdapter;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetExecutionStatusUseCaseImpl implements GetExecutionStatusUseCase {

	private CacheAdapter cacheAdapter;
	@Override
	public AggregationResultDTO getExecutionStatus(Long executionId) {
		// TODO Auto-generated method stub
		return cacheAdapter.getAggregationResult(executionId);
	}

}
