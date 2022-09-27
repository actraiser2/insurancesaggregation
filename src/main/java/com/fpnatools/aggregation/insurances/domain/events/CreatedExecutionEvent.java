package com.fpnatools.aggregation.insurances.domain.events;

import java.util.Map;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.Execution;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatedExecutionEvent implements DomainEvent {
	private Execution execution;
	private Map<String, String> credentials;
}
