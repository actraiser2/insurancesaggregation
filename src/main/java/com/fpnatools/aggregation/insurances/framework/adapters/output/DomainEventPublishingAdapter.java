package com.fpnatools.aggregation.insurances.framework.adapters.output;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fpnatools.aggregation.insurances.domain.events.CreatedExecutionEvent;
import com.fpnatools.aggregation.insurances.domain.events.UpdatedExecutionEvent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j

public class DomainEventPublishingAdapter {

	private MessageChannel executionsChannel;
	
	@TransactionalEventListener()
	public void on(CreatedExecutionEvent event) {
		log.info("Event captured:" + event.getExecution().getId());
		executionsChannel.send(MessageBuilder.withPayload(event).build());
		//processExecutionUseCase.processExecution(event.getExecution().getId(), event.getCredentials());
	}
	
	@TransactionalEventListener
	public void on (UpdatedExecutionEvent event) {
		log.info("Event execution finished:" + event.getExecution().getId());	
	}
}
