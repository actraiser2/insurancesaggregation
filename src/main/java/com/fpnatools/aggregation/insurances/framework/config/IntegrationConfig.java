package com.fpnatools.aggregation.insurances.framework.config;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;

import com.fpnatools.aggregation.insurances.application.usecases.ProcessExecutionUseCase;

@Configuration
public class IntegrationConfig {

	@Bean
	public MessageChannel executionsChannel() {
		return MessageChannels.executor(Executors.newFixedThreadPool(10)).get();
	}
	
	@Bean
	public IntegrationFlow executionsFlow(ProcessExecutionUseCase processExecutionUseCase) {
		return IntegrationFlows.from(executionsChannel()).
				handle("ProcessExecutionUseCase", "processExecution").get();
	}
}
