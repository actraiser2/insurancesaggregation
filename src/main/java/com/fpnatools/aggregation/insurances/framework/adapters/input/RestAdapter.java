package com.fpnatools.aggregation.insurances.framework.adapters.input;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpnatools.aggregation.insurances.application.usecases.CreateExecutionUseCase;
import com.fpnatools.aggregation.insurances.application.usecases.GetExecutionStatusUseCase;
import com.fpnatools.aggregation.insurances.domain.vo.AggregationResult;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionDTO;
import com.fpnatools.aggregation.insurances.framework.adapters.input.dto.ExecutionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/executions")
@AllArgsConstructor
@SecurityRequirement(name = "Basic")
public class RestAdapter {

	private CreateExecutionUseCase createExecutionUseCase;
	private GetExecutionStatusUseCase getExecutionStatusUseCase;
	
	private static Logger logger = LoggerFactory.getLogger(RestAdapter.class);
	
	@PostMapping
	public ResponseEntity<ExecutionResponseDTO> createExecution(@Valid @RequestBody ExecutionDTO execution, Authentication auth) {
		Long executionId = createExecutionUseCase.createExecution(auth.getName(), execution);
		return ResponseEntity.status(201).body(new ExecutionResponseDTO(executionId));
	}
	
	@GetMapping("/{executionId}")
	@Operation(summary = "Get the status of the execution")
	public AggregationResult getExecutionStatus(
			@Parameter(description = "Identification of the execution") @PathVariable("executionId") Long executionId) {
		return getExecutionStatusUseCase.getExecutionStatus(executionId);
	}
	
	@GetMapping("/echo")
	public String echo(JwtAuthenticationToken auth) {
		
		logger.info("cALLING METHOD ECHO");
		return auth.getToken().getTokenValue();
	}
}
