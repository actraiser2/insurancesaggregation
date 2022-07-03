package com.fpnatools.aggregation.insurances.framework.restAPI;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpnatools.aggregation.insurances.application.usecases.CreateExecutionUseCase;
import com.fpnatools.aggregation.insurances.application.usecases.GetExecutionStatusUseCase;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.AggregationResultDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.ExecutionRequestDTO;
import com.fpnatools.aggregation.insurances.framework.restAPI.dto.ExecutionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/executions")
@AllArgsConstructor
@SecurityRequirement(name = "Basic")
public class ExecutionController {

	private CreateExecutionUseCase createExecutionUseCase;
	private GetExecutionStatusUseCase getExecutionStatusUseCase;
	
	private static Logger logger = LoggerFactory.getLogger(ExecutionController.class);
	
	@PostMapping
	public ResponseEntity<ExecutionResponseDTO> createExecution(@Valid @RequestBody ExecutionRequestDTO execution, Authentication auth) {
		Long executionId = createExecutionUseCase.createExecution(auth.getName(), execution);
		return ResponseEntity.status(201).body(new ExecutionResponseDTO(executionId));
	}
	
	@GetMapping("/{executionId}")
	@Operation(summary = "Get the status of the execution")
	public AggregationResultDTO getExecutionStatus(
			@Parameter(description = "Identification of the execution") @PathVariable("executionId") Long executionId) {
		return getExecutionStatusUseCase.getExecutionStatus(executionId);
	}
	
	@GetMapping("/echo")
	public String echo(JwtAuthenticationToken auth) {
		String cad = """
				Hi roman,
				
				I greet to you
				""";
		logger.info("cALLING METHOD ECHO");
		return auth.getToken().getTokenValue();
	}
}
