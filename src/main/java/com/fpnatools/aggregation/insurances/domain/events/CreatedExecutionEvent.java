package com.fpnatools.aggregation.insurances.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatedExecutionEvent {

	private CreatedExecutionEventData data;
}
