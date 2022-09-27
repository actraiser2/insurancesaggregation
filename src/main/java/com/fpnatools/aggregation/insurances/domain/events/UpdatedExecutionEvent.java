package com.fpnatools.aggregation.insurances.domain.events;

import com.fpnatools.aggregation.insurances.domain.model.aggregates.Execution;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedExecutionEvent implements DomainEvent {

	private Execution execution;
}
