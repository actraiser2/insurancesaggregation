package com.fpnatools.aggregation.insurances.framework.adapters.input;

import javax.validation.Valid;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpnatools.aggregation.insurances.application.usecases.CreateExecutionUseCase;
import com.fpnatools.aggregation.insurances.application.usecases.GetExecutionStatusUseCase;
import com.fpnatools.aggregation.insurances.domain.model.aggregates.valueobjects.AggregationResult;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.CreateExecutionDTO;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/executions")
@AllArgsConstructor
@SecurityRequirement(name = "Basic")
@Slf4j
public class RestAdapter {

	private CreateExecutionUseCase createExecutionUseCase;
	private GetExecutionStatusUseCase getExecutionStatusUseCase;
	private Environment env;
	
	@PostMapping
	public Mono<ResponseEntity<ExecutionResponseDTO>> createExecution(@Valid @RequestBody CreateExecutionDTO createExecutionDTO, Authentication auth) {
		log.info("Creating execution ");
		Long executionId = createExecutionUseCase.createExecution(auth.getName(), createExecutionDTO);
		return Mono.just(ResponseEntity.status(201).body(new ExecutionResponseDTO(executionId)));
	}
	
	@GetMapping(value = "/{executionId}")
	@Operation(summary = "Get the status of the execution")
	public Mono<AggregationResult> getExecutionStatus(
			@Parameter(description = "Identification of the execution") @PathVariable("executionId") Long executionId) {
		log.info("Querying execution ");
		return Mono.fromCallable(() -> getExecutionStatusUseCase.getExecutionStatus(executionId)).publishOn(Schedulers.boundedElastic());
	}
	
}
